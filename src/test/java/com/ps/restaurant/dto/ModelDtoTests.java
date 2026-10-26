package com.ps.restaurant.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ps.restaurant.model.ItemSize;
import com.ps.restaurant.model.OrderStatus;
import com.ps.restaurant.model.PaymentMethod;

import jakarta.persistence.Entity;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import tools.jackson.databind.json.JsonMapper;

/** Checks request constraints and JSON contracts without requiring a database. */
class ModelDtoTests {

    private static final BigDecimal PRICE = new BigDecimal("12.50");
    private static final Instant CREATED_AT = Instant.parse("2026-10-26T12:00:00Z");
    private static final JsonMapper JSON = JsonMapper.builder().findAndAddModules().build();

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @ParameterizedTest
    @MethodSource("validRequests")
    void acceptsValidRequests(Object request) {
        assertThat(validator.validate(request)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void rejectsInvalidRequestFields(Object request, List<String> fields) {
        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsAll(fields);
    }

    @ParameterizedTest
    @MethodSource({"validRequests", "responses"})
    void preservesDtoFieldsWhenSerializedAndDeserialized(Object dto) throws Exception {
        String json = JSON.writeValueAsString(dto);

        assertThat(JSON.readValue(json, dto.getClass())).isEqualTo(dto);
    }

    @ParameterizedTest
    @MethodSource({"validRequests", "responses"})
    void doesNotExposeJpaEntities(Object dto) {
        assertThat(Arrays.stream(dto.getClass().getRecordComponents())
                .map(component -> component.getType())
                .filter(type -> type.isAnnotationPresent(Entity.class)))
                .isEmpty();
    }

    @Test
    void serializesOrderWithChildDtosAndRelationshipIdentifiers() throws Exception {
        String json = JSON.writeValueAsString(orderResponse());

        assertThat(json).contains("\"waiterId\":2", "\"tableId\":3", "\"orderId\":1",
                "\"productId\":4", "\"size\":\"WHOLE\"", "\"method\":\"CARD\"",
                "\"status\":\"OPEN\"", "\"createdAt\":\"2026-10-26T12:00:00Z\"");
        assertThat(json).doesNotContain("\"password\"", "\"waiter\"", "\"order\"", "\"product\"");
    }

    static Stream<Object> validRequests() {
        return Stream.of(
                new CategoryRequest("Food"),
                new RoleRequest("WAITER", true),
                new TableRequest(1, 4),
                new TableRequest(2, null),
                new ProductRequest("FOOD-1", "Test product", PRICE, 1L, true),
                new ProductRequest("FREE-1", "Complimentary item", BigDecimal.ZERO, 1L, false),
                new OrderRequest(UUID.randomUUID(), 2L, "Ana", List.of("nuts", "milk", "nuts"),
                        3L, OrderStatus.OPEN, false, PRICE),
                new OrderRequest(UUID.randomUUID(), 2L, "Takeaway", null,
                        null, OrderStatus.OPEN, true, BigDecimal.ZERO),
                new OrderItemRequest(1L, 4L, 1, ItemSize.WHOLE,
                        List.of("no onion", "extra cheese"), PRICE, "No salt"),
                new OrderItemRequest(1L, 4L, 1, ItemSize.HALF, null, BigDecimal.ZERO, null),
                new PaymentRequest(1L, PRICE, PaymentMethod.CARD));
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                Arguments.of(new CategoryRequest(" "), List.of("name")),
                Arguments.of(new CategoryRequest("a".repeat(256)), List.of("name")),
                Arguments.of(new RoleRequest(null, false), List.of("name")),
                Arguments.of(new TableRequest(null, 0), List.of("tableNumber", "capacity")),
                Arguments.of(new TableRequest(0, -1), List.of("tableNumber", "capacity")),
                Arguments.of(new ProductRequest("", " ", null, null, true),
                        List.of("sku", "name", "price", "categoryId")),
                Arguments.of(new ProductRequest("SKU", "Product", new BigDecimal("-0.01"), 0L, true),
                        List.of("price", "categoryId")),
                Arguments.of(new ProductRequest("SKU", "Product", new BigDecimal("1.001"), 1L, true),
                        List.of("price")),
                Arguments.of(new ProductRequest("SKU", "Product", new BigDecimal("10000000000.00"), 1L, true),
                        List.of("price")),
                Arguments.of(new OrderRequest(null, null, " ", null, -1L, null, false, null),
                        List.of("orderNumber", "waiterId", "customer", "tableId", "status", "total")),
                Arguments.of(new OrderRequest(UUID.randomUUID(), 1L, "Ana", List.of(" "), null,
                        OrderStatus.OPEN, true, new BigDecimal("-1.00")),
                        List.of("allergies[0].<list element>", "total")),
                Arguments.of(new OrderItemRequest(null, null, null, null, null, null, null),
                        List.of("orderId", "productId", "quantity", "size", "unitPrice")),
                Arguments.of(new OrderItemRequest(0L, -1L, 0, ItemSize.SMALL, List.of(""),
                        new BigDecimal("-1.00"), "a".repeat(256)),
                        List.of("orderId", "productId", "quantity", "attributes[0].<list element>",
                                "unitPrice", "notes")),
                Arguments.of(new PaymentRequest(null, null, null), List.of("orderId", "amount", "method")),
                Arguments.of(new PaymentRequest(0L, BigDecimal.ZERO, PaymentMethod.CASH),
                        List.of("orderId", "amount")),
                Arguments.of(new PaymentRequest(1L, new BigDecimal("-0.01"), PaymentMethod.TRANSFER),
                        List.of("amount")),
                Arguments.of(new PaymentRequest(1L, new BigDecimal("1.001"), PaymentMethod.CARD),
                        List.of("amount")));
    }

    static Stream<Object> responses() {
        return Stream.of(
                new CategoryResponse(1L, "Food"),
                new RoleResponse(1L, "WAITER", true),
                new TableResponse(3L, 1, 4),
                new ProductResponse(4L, "FOOD-1", "Test product", PRICE, 1L, true),
                itemResponse(),
                paymentResponse(),
                orderResponse(),
                new OrderResponse(2L, UUID.randomUUID(), 2L, "Takeaway", List.of(), null,
                        List.of(), List.of(), OrderStatus.OPEN, true, BigDecimal.ZERO, CREATED_AT));
    }

    private static OrderItemResponse itemResponse() {
        return new OrderItemResponse(5L, 1L, 4L, 1, ItemSize.WHOLE,
                List.of("no onion", "extra cheese"), PRICE, "No salt", CREATED_AT);
    }

    private static PaymentResponse paymentResponse() {
        return new PaymentResponse(6L, 1L, PRICE, PaymentMethod.CARD, CREATED_AT);
    }

    private static OrderResponse orderResponse() {
        return new OrderResponse(1L, UUID.randomUUID(), 2L, "Ana", List.of("nuts", "milk", "nuts"),
                3L, List.of(itemResponse()), List.of(paymentResponse()), OrderStatus.OPEN, false, PRICE, CREATED_AT);
    }
}
