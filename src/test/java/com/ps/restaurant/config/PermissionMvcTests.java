package com.ps.restaurant.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
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
