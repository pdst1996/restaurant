package com.ps.restaurant.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ps.restaurant.dto.filters.FilterOrderRequest;
import com.ps.restaurant.dto.OrderResponse;
import com.ps.restaurant.dto.OrderRequest;
import com.ps.restaurant.config.GlobalExceptionHandler;
import com.ps.restaurant.interfaces.OrderService;
import com.ps.restaurant.model.ItemSize;
import com.ps.restaurant.model.Allergy;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderItem;
import com.ps.restaurant.model.OrderStatus;
import com.ps.restaurant.model.Payment;
import com.ps.restaurant.model.PaymentMethod;
import com.ps.restaurant.model.Item;
import com.ps.restaurant.model.Table;
import com.ps.restaurant.model.User;

@ExtendWith(MockitoExtension.class)
class OrderControllerTests {

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService, null))
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseHandler()), new ErrorResponseHandler())
                .build();
    }

    @Test
    void returnsOrdersFilteredByIdWithChildDtos() throws Exception {
        Order order = order();
        when(orderService.filterOrders(any())).thenReturn(List.of(OrderResponse.from(order)));

        var result = mockMvc.perform(get("/order/orders").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderNumber").value(order.getOrderNumber()))
                .andExpect(jsonPath("$[0].user.id").value(2))
                .andExpect(jsonPath("$[0].table.id").value(3))
                .andExpect(jsonPath("$[0].items[0].item.id").value(4))
                .andExpect(jsonPath("$[0].payments[0].orderId").value(1))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("\"password\"", "\"waiter\"", "\"order\"", "\"product\"");
        verify(orderService).filterOrders(new FilterOrderRequest(1L, null, null, null, null, null, null));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void returnsEmptyListForMissingOrderId() throws Exception {
        when(orderService.filterOrders(any())).thenReturn(List.of());

        mockMvc.perform(get("/order/orders").param("id", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(orderService).filterOrders(new FilterOrderRequest(99L, null, null, null, null, null, null));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void rejectsNegativeFilterId() throws Exception {
        mockMvc.perform(get("/order/orders").param("id", "-1")).andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void rejectsNonNumericFilterId() throws Exception {
        mockMvc.perform(get("/order/orders").param("id", "invalid")).andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void bindsAllFiltersAndReturnsOrderWithChildDtos() throws Exception {
        Order order = order();
        when(orderService.filterOrders(any())).thenReturn(List.of(OrderResponse.from(order)));

        var result = mockMvc.perform(get("/order/orders")
                .param("id", "1")
                .param("orderNumber", order.getOrderNumber())
                .param("status", "OPEN")
                .param("tableId", "3")
                .param("waiterId", "2")
                .param("fromDate", "2026-10-26T00:00:00Z")
                .param("toDate", "2026-10-27T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderNumber").value(order.getOrderNumber()))
                .andExpect(jsonPath("$[0].user.id").value(2))
                .andExpect(jsonPath("$[0].table.id").value(3))
                .andExpect(jsonPath("$[0].customer").value("Ana"))
                .andExpect(jsonPath("$[0].allergies[0].allergyKey").value("nuts"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].togo").value(false))
                .andExpect(jsonPath("$[0].total").value(25.00))
                .andExpect(jsonPath("$[0].createdAt").value(order.getCreatedAt().toString()))
                .andExpect(jsonPath("$[0].items[0].id").value(5))
                .andExpect(jsonPath("$[0].items[0].orderId").value(1))
                .andExpect(jsonPath("$[0].items[0].item.id").value(4))
                .andExpect(jsonPath("$[0].items[0].quantity").value(2))
                .andExpect(jsonPath("$[0].items[0].size.sizeKey").value("FULL"))
                .andExpect(jsonPath("$[0].items[0].attributes[0].attributeKey").value("NO_ONION"))
                .andExpect(jsonPath("$[0].items[0].unitPrice").value(12.50))
                .andExpect(jsonPath("$[0].items[0].notes").value("No salt"))
                .andExpect(jsonPath("$[0].payments[0].id").value(6))
                .andExpect(jsonPath("$[0].payments[0].orderId").value(1))
                .andExpect(jsonPath("$[0].payments[0].amount").value(25.00))
                .andExpect(jsonPath("$[0].payments[0].method").value("CARD"))
                .andReturn();

        verify(orderService).filterOrders(new FilterOrderRequest(
                1L, order.getOrderNumber(), OrderStatus.OPEN, 3L, 2L,
                Instant.parse("2026-10-26T00:00:00Z"), Instant.parse("2026-10-27T00:00:00Z")));
        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("\"password\"", "\"waiter\"", "\"order\"", "\"product\"");
    }

    @Test
    void returnsTakeawayOrderWithoutTableOrChildren() throws Exception {
        Order order = order();
        order.setTable(null);
        order.setTogo(true);
        order.getItems().clear();
        order.getPayments().clear();
        when(orderService.filterOrders(any())).thenReturn(List.of(OrderResponse.from(order)));

        mockMvc.perform(get("/order/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].table").isEmpty())
                .andExpect(jsonPath("$[0].togo").value(true))
                .andExpect(jsonPath("$[0].items").isEmpty())
                .andExpect(jsonPath("$[0].payments").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"111111", "000001", "ORD-2026-001"})
    void bindsStringOrderNumberFiltersWithoutUuidConversion(String number) throws Exception {
        Order order = order();
        order.setOrderNumber(number);
        when(orderService.filterOrders(any())).thenReturn(List.of(OrderResponse.from(order)));

        mockMvc.perform(get("/order/orders").param("orderNumber", number))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value(number));

        verify(orderService).filterOrders(new FilterOrderRequest(null, number, null, null, null, null, null));
    }

    @Test
    void returnsEmptyListWhenNoOrdersMatch() throws Exception {
        when(orderService.filterOrders(any())).thenReturn(List.of());

        mockMvc.perform(get("/order/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(orderService).filterOrders(new FilterOrderRequest(null, null, null, null, null, null, null));
    }

    @Test
    void rejectsNonPositiveOrderId() throws Exception {
        mockMvc.perform(get("/order/orders").param("id", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void countsOrdersUsingFilters() throws Exception {
        when(orderService.countOrders(any())).thenReturn(1L);

        mockMvc.perform(get("/order/orders/count").param("status", "OPEN").param("tableId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));

        verify(orderService).countOrders(new FilterOrderRequest(null, null, OrderStatus.OPEN, 3L, null, null, null));
    }

    @Test
    void rejectsInvalidCountFilters() throws Exception {
        mockMvc.perform(get("/order/orders/count").param("tableId", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createsOrderFromJsonWithBooleanTogo(boolean togo) throws Exception {
        Order order = order();
        order.setTogo(togo);
        when(orderService.createOrder(any())).thenReturn(OrderResponse.from(order));

        mockMvc.perform(post("/order/orders").contentType(MediaType.APPLICATION_JSON).content("""
                {"orderNumber":"111111","waiterId":2,"customer":"Ana","tableId":3,
                 "allergyIds":[7],"status":"OPEN","togo":%s,"total":25.00}
                """.formatted(togo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.togo").value(togo))
                .andExpect(jsonPath("$.allergies[0].allergyKey").value("nuts"));

        verify(orderService).createOrder(new OrderRequest(
                "111111", 2L, "Ana", 3L, List.of(7L), OrderStatus.OPEN, togo, new BigDecimal("25.00"), null, null));
    }

    @Test
    void createsOrderFromMinimalJsonWithoutTogo() throws Exception {
        when(orderService.createOrder(any())).thenReturn(OrderResponse.from(order()));

        mockMvc.perform(post("/order/orders").contentType(MediaType.APPLICATION_JSON).content("""
                {"orderNumber":"SEED-ORD-232344","waiterId":1}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.togo").value(false));

        verify(orderService).createOrder(new OrderRequest(
                "SEED-ORD-232344", 1L, null, null, null, null, false, null, null, null));
    }

    @Test
    void returnsStackTraceForMalformedOrderJson() throws Exception {
        mockMvc.perform(post("/order/orders").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.origin").value("/order/orders"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());

        verifyNoInteractions(orderService);
    }

    @Test
    void rejectsInvalidOrderBeforeCallingService() throws Exception {
        mockMvc.perform(post("/order/orders").contentType(MediaType.APPLICATION_JSON).content("""
                {"orderNumber":"111111","waiterId":0,"togo":true}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_EXCEPTION"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());

        verifyNoInteractions(orderService);
    }

    private Order order() {
        User waiter = new User();
        waiter.setId(2L);
        waiter.setRole(new com.ps.restaurant.model.Role());
        waiter.setPassword("must-not-be-exposed");
        Table table = new Table();
        table.setId(3L);
        table.setFloor(new com.ps.restaurant.model.Floor());
        Item product = new Item();
        product.setId(4L);
        product.setCategory(new com.ps.restaurant.model.ItemCategory());

        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("111111");
        order.setStatus(OrderStatus.OPEN);
        order.setWaiter(waiter);
        order.setTable(table);
        order.setCustomer("Ana");
        Allergy allergy = new Allergy();
        allergy.setId(7L);
        allergy.setAllergyKey("nuts");
        order.setAllergies(List.of(allergy));
        order.setTotal(new BigDecimal("25.00"));
        order.setCreatedAt(Instant.parse("2026-10-26T12:00:00Z"));

        OrderItem item = new OrderItem();
        item.setId(5L);
        item.setOrder(order);
        item.setItem(product);
        item.setQuantity(2);
        ItemSize size = new ItemSize();
        size.setId(8L);
        size.setSizeKey("FULL");
        item.setSize(size);
        com.ps.restaurant.model.ItemAttribute attribute = new com.ps.restaurant.model.ItemAttribute();
        attribute.setAttributeKey("NO_ONION");
        item.setAttributes(List.of(attribute));
        item.setUnitPrice(new BigDecimal("12.50"));
        item.setNotes("No salt");
        order.getItems().add(item);

        Payment payment = new Payment();
        payment.setId(6L);
        payment.setOrder(order);
        payment.setAmount(order.getTotal());
        payment.setMethod(PaymentMethod.CARD);
        order.getPayments().add(payment);
        return order;
    }
}
