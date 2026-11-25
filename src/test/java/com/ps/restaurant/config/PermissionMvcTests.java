package com.ps.restaurant.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import com.ps.restaurant.model.Item;
import com.ps.restaurant.model.ItemCategory;
import com.ps.restaurant.model.ItemSize;
import jakarta.persistence.EntityManager;
import tools.jackson.databind.json.JsonMapper;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.RoleRepository;
import com.ps.restaurant.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PermissionMvcTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired JwtService jwt;
    @Autowired EntityManager em;

    @Test
    void distinguishesMissingAuthenticationAndRoleGrants() throws Exception {
        mvc.perform(get("/order/orders")).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/register")).andExpect(status().isUnauthorized());
        mvc.perform(get("/order/orders").header("Authorization", token("mesero"))).andExpect(status().isOk());
        mvc.perform(get("/payment/payments").header("Authorization", token("mesero"))).andExpect(status().isForbidden());
        mvc.perform(get("/user/users").header("Authorization", token("gerente"))).andExpect(status().isForbidden());
        mvc.perform(get("/payment/payments").header("Authorization", token("cajero"))).andExpect(status().isOk());
        mvc.perform(get("/role/roles/permissions").header("Authorization", token("admin"))).andExpect(status().isOk());
        mvc.perform(post("/order/orders").header("Authorization", token("cocina"))).andExpect(status().isForbidden());
    }

    @Test
    void existingTokenLosesAccessWhenAccountIsDisabled() throws Exception {
        String token = token("cocina");
        User user = users.findByEmail("test-cocina@example.com").orElseThrow();
        user.setActive(false);
        users.flush();
        mvc.perform(get("/order/orders").header("Authorization", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void transitionsThroughTheCorrectRolesAndRecordsWhoChangedWhat() throws Exception {
        String waiter = token("mesero");
        String cook = token("cocina");
        Long waiterId = users.findByEmail("test-mesero@example.com").orElseThrow().getId();
        Long cookId = users.findByEmail("test-cocina@example.com").orElseThrow().getId();
        ItemCategory category = new ItemCategory();
        category.setCategoryKey("WORKFLOW");
        category.setDisplayName(Map.of("en", "Workflow"));
        em.persist(category);
        ItemSize size = new ItemSize();
        size.setSizeKey("WORKFLOW");
        size.setDisplayName(Map.of("en", "Full"));
        em.persist(size);
        Item product = new Item();
        product.setSku("WORKFLOW");
        product.setDisplayName(Map.of("en", "Test item"));
        product.setCategory(category);
        product.setPrice(new BigDecimal("100.00"));
        product.getSizes().add(size);
        em.persist(product);

        String json = mvc.perform(post("/order/orders").header("Authorization", waiter)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNumber\":\"WORKFLOW\",\"waiterId\":" + waiterId + "}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long id = JsonMapper.builder().build().readTree(json).get("id").asLong();
        String path = "/order/orders/" + id;
        mvc.perform(post("/order-item/order-items").header("Authorization", waiter)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":" + id + ",\"itemId\":" + product.getId() + ",\"sizeId\":"
                                + size.getId() + ",\"quantity\":1,\"unitPrice\":0.01}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.unitPrice").value(100));
        transition(path, waiter, "OPEN").andExpect(status().isOk());
        transition(path, cook, "READY").andExpect(status().isConflict());
        transition(path, waiter, "PREPARING").andExpect(status().isForbidden());
        transition(path, cook, "PREPARING").andExpect(status().isOk());
        transition(path, cook, "READY").andExpect(status().isOk());
        transition(path, waiter, "SERVED").andExpect(status().isOk()).andExpect(jsonPath("$.total").value(100));
        mvc.perform(post(path + "/complete").header("Authorization", token("cajero"))).andExpect(status().isConflict());
        mvc.perform(get(path + "/history").header("Authorization", waiter))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[2].fromStatus").value("OPEN"))
                .andExpect(jsonPath("$[2].toStatus").value("PREPARING"))
                .andExpect(jsonPath("$[2].actorId").value(cookId));

        String cashier = token("cajero");
        mvc.perform(get(path + "/balance").header("Authorization", cashier))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(100));
        String cash = "{\"orderId\":" + id + ",\"amount\":40,\"method\":\"CASH\",\"tip\":5,\"cashReceived\":50}";
        String cashJson = pay(cashier, "cash-01", cash)
                .andExpect(status().isOk()).andExpect(jsonPath("$.change").value(5))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn().getResponse().getContentAsString();
        long paymentId = JsonMapper.builder().build().readTree(cashJson).get("id").asLong();
        pay(cashier, "cash-01", cash).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(paymentId));
        pay(cashier, "cash-01", cash.replace("\"amount\":40", "\"amount\":41")).andExpect(status().isConflict());
        mvc.perform(get(path + "/balance").header("Authorization", cashier))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(60))
                .andExpect(jsonPath("$.paid").value(40)).andExpect(jsonPath("$.tip").value(5));
        mvc.perform(get("/payment/payments/count").param("orderId", String.valueOf(id)).header("Authorization", cashier))
                .andExpect(status().isOk()).andExpect(jsonPath("$").value(1));

        String card = "{\"orderId\":" + id + ",\"amount\":60,\"method\":\"CARD\"}";
        pay(cashier, "overpay-01", card.replace("\"amount\":60", "\"amount\":61")).andExpect(status().isConflict());
        pay(cashier, "mock-decline", card.replace("}", ",\"mockOutcome\":\"DECLINE\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FAILED"));
        pay(cashier, "card-01", card).andExpect(status().isOk()).andExpect(jsonPath("$.externalReference").isString());
        mvc.perform(post(path + "/complete").header("Authorization", cashier))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAID"));
        String refundPath = "/payment/payments/" + paymentId + "/refund";
        String refund = "{\"reason\":\"Customer request\"}";
        mvc.perform(post(refundPath).header("Authorization", cashier).contentType(MediaType.APPLICATION_JSON)
                .content(refund)).andExpect(status().isForbidden());
        String manager = token("gerente");
        mvc.perform(post(refundPath).header("Authorization", manager).contentType(MediaType.APPLICATION_JSON)
                .content(refund)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REFUNDED"))
                .andExpect(jsonPath("$.refundReason").value("Customer request"))
                .andExpect(jsonPath("$.refundedBy").isNumber()).andExpect(jsonPath("$.refundedAt").exists());
        mvc.perform(get(path + "/balance").header("Authorization", cashier))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(40))
                .andExpect(jsonPath("$.tip").value(0));
        mvc.perform(get("/order/orders").param("id", String.valueOf(id)).header("Authorization", cook))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("SERVED"))
                .andExpect(jsonPath("$[0].payments").isEmpty());
    }

    private org.springframework.test.web.servlet.ResultActions pay(String token, String key, String body) throws Exception {
        return mvc.perform(post("/payment/payments").header("Authorization", token).header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private org.springframework.test.web.servlet.ResultActions transition(String path, String token, String next) throws Exception {
        return mvc.perform(post(path + "/status").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + next + "\"}"));
    }

    private String token(String role) {
        User user = users.findByEmail("test-" + role + "@example.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("test-" + role + "@example.com");
            u.setUser("test-" + role);
            u.setFullName(role);
            u.setPassword("unused");
            u.setActive(true);
            u.setRole(roles.findByName(role).orElseThrow());
            return users.saveAndFlush(u);
        });
        return "Bearer " + jwt.generateToken(user.getEmail());
    }
}
