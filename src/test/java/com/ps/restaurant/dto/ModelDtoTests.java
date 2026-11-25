package com.ps.restaurant.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ps.restaurant.model.Allergy;
import com.ps.restaurant.model.Item;
import com.ps.restaurant.model.ItemAttribute;
import com.ps.restaurant.model.ItemCategory;
import com.ps.restaurant.model.ItemSize;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderItem;
import com.ps.restaurant.model.OrderStatus;
import com.ps.restaurant.model.PaymentMethod;
import com.ps.restaurant.model.User;

import jakarta.persistence.Entity;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import tools.jackson.databind.json.JsonMapper;

/** Checks request constraints and JSON contracts without requiring a database. */
class ModelDtoTests {

    private static final BigDecimal PRICE = new BigDecimal("12.50");
    private static final Instant CREATED_AT = Instant.parse("2026-10-26T12:00:00Z");
    private static final Map<String, String> NAMES = Map.of("es", "Pollo", "en", "Chicken");
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
                .extracting(violation -> violation.getPropertyPath().iterator().next().getName())
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
        Arrays.stream(dto.getClass().getRecordComponents())
                .forEach(component -> assertNotEntity(component.getGenericType()));
    }

    @Test
    void serializesOrderWithChildDtosAndRelationshipIdentifiers() throws Exception {
        String json = JSON.writeValueAsString(orderResponse());

        assertThat(json).contains("\"user\":{\"id\":2", "\"table\":{\"id\":3", "\"orderId\":1",
                "\"item\":{\"id\":4", "\"sizeKey\":\"FULL\"", "\"allergyKey\":\"NUTS\"", "\"method\":\"CARD\"",
                "\"status\":\"OPEN\"", "\"createdAt\":\"2026-10-26T12:00:00Z\"");
        assertThat(json).doesNotContain("\"password\"", "\"waiter\"", "\"order\"", "\"product\"");
    }

    static Stream<Object> validRequests() {
        return Stream.of(
                new CategoryRequest("FOOD", NAMES),
                new AllergyRequest("NUTS", NAMES),
                new ItemAttributeRequest("NO_ONION", NAMES),
                new ItemSizeRequest("FULL", NAMES),
                new RoleRequest("WAITER", true),
                new RoleReferenceRequest(1L),
                new UserRequest("ana@example.com", "password", "ana", "Ana", new RoleReferenceRequest(1L)),
                new FloorRequest("Piso 1", null, null, null),
                new FloorRequest("Terraza", 1200, 800, 0),
                new ElementTypeRequest("wall", NAMES, 200, 12, false, "#fff", "#000"),
                new ElementPlanRequest(1L, "wall", 0, 0, 200, 12, null, null),
                new LayoutRequest(null, null, null),
                new LayoutRequest(List.of(new TableLayoutRequest(3L,
                        new TableRequest("1", 4, 1L, 0, 0, null, null, null, null))),
                        List.of(new ElementLayoutRequest(null,
                                new ElementPlanRequest(1L, "wall", 0, 0, 200, 12, 0, null))), List.of(2L)),
                new TableRequest("1", 4, 1L, 0, 0, 100, 80, 0, com.ps.restaurant.model.TableShape.RECTANGULAR),
                new TableRequest("2", null, 1L, null, null, null, null, null, null),
                new ItemRequest("FOOD-1", NAMES, Map.of("ean", "123456789", "codes", List.of("ABC", "DEF")),
                        "Roast chicken", PRICE, 1L, true, "chicken.jpg", List.of(6L), List.of(7L)),
                new ItemRequest("FREE-1", NAMES, null, null, BigDecimal.ZERO, 1L, false, null, null, null),
                new OrderRequest("111111", 2L, "Ana", 3L, List.of(8L, 9L),
                        OrderStatus.OPEN, false, PRICE, null, null),
                new OrderRequest("000001", 2L, "Takeaway", null,
                        null, OrderStatus.OPEN, true, BigDecimal.ZERO, null, null),
                new OrderRequest("ORD-" + "a".repeat(251), 2L, "Ana", null,
                        null, OrderStatus.OPEN, true, PRICE, null, null),
                new OrderRequest("DEFAULTS", 2L, "Ana", null, null, null, false, null, null, null),
                new OrderItemRequest(1L, 4L, 1, 7L,
                        List.of(6L, 9L), PRICE, "No salt"),
                new OrderItemRequest(1L, 4L, 1, 7L, null, BigDecimal.ZERO, null),
                new PaymentRequest(1L, PRICE, PaymentMethod.CARD));
    }

    static Stream<Arguments> invalidRequests() {
        return Stream.of(
                Arguments.of(new CategoryRequest(" ", NAMES), List.of("categoryKey")),
                Arguments.of(new CategoryRequest("a".repeat(256), NAMES), List.of("categoryKey")),
                Arguments.of(new CategoryRequest("FOOD", Map.of()), List.of("displayName")),
                Arguments.of(new AllergyRequest(null, null), List.of("allergyKey", "displayName")),
                Arguments.of(new AllergyRequest("a".repeat(256), NAMES), List.of("allergyKey")),
                Arguments.of(new ItemAttributeRequest(" ", Map.of("es", " ")),
                        List.of("attributeKey", "displayName")),
                Arguments.of(new ItemAttributeRequest("a".repeat(256), NAMES), List.of("attributeKey")),
                Arguments.of(new ItemSizeRequest(null, Map.of("", "Name")), List.of("sizeKey", "displayName")),
                Arguments.of(new ItemSizeRequest("a".repeat(256), NAMES), List.of("sizeKey")),
                Arguments.of(new RoleRequest(null, false), List.of("name")),
                Arguments.of(new RoleReferenceRequest(0L), List.of("id")),
                Arguments.of(new UserRequest("invalid", "", "", "", new RoleReferenceRequest(null)),
                        List.of("email", "password", "user", "fullName", "role")),
                Arguments.of(new FloorRequest(" ", 0, -1, -1), List.of("name", "width", "height", "sort_order")),
                Arguments.of(new ElementTypeRequest("", Map.of(), 0, null, false, null, null),
                        List.of("type", "label", "width", "height")),
                Arguments.of(new ElementPlanRequest(null, "", -1, -1, 0, 0, null, null),
                        List.of("floorId", "type", "x", "y", "width", "height")),
                Arguments.of(new LayoutRequest(null, null, List.of(0L)), List.of("deletedElementIds")),
                Arguments.of(new LayoutRequest(List.of(new TableLayoutRequest(-1L, null)), null, null), List.of("tables")),
                Arguments.of(new LayoutRequest(null, List.of(new ElementLayoutRequest(-1L, null)), null), List.of("elements")),
                Arguments.of(new TableRequest(null, 0, null, -1, -1, 0, 0, null, null),
                        List.of("tableName", "capacity", "floorId", "x", "y", "width", "height")),
                Arguments.of(new TableRequest(" ", -1, -1L, null, null, null, null, null, null),
                        List.of("tableName", "capacity", "floorId")),
                Arguments.of(new ItemRequest("", Map.of(), null, null, null, null, true, null, null, null),
                        List.of("sku", "displayName", "price", "categoryId")),
                Arguments.of(new ItemRequest("SKU", NAMES, null, null, new BigDecimal("-0.01"), 0L, true, null, null, null),
                        List.of("price", "categoryId")),
                Arguments.of(new ItemRequest("SKU", NAMES, null, null, new BigDecimal("1.001"), 1L, true, null, null, null),
                        List.of("price")),
                Arguments.of(new ItemRequest("SKU", NAMES, null, null, new BigDecimal("10000000000.00"), 1L, true, null, null, null),
                        List.of("price")),
                Arguments.of(new ItemRequest("SKU", Map.of("es", " "), Map.of("", "123"), "a".repeat(256),
                        PRICE, 1L, true, null, List.of(0L), Arrays.asList((Long) null)),
                        List.of("displayName", "barcodes", "description", "attributeIds", "sizeIds")),
                Arguments.of(new OrderRequest(null, null, "a".repeat(256), -1L, null, null, false, null, null, null),
                        List.of("orderNumber", "waiterId", "customer", "tableId")),
                Arguments.of(new OrderRequest("111111", 1L, "Ana", null, List.of(0L),
                        OrderStatus.OPEN, true, new BigDecimal("-1.00"), null, null),
                        List.of("allergyIds", "total")),
                Arguments.of(new OrderRequest("111111", 1L, "Ana", null, Arrays.asList((Long) null),
                        OrderStatus.OPEN, true, PRICE, null, null), List.of("allergyIds")),
                Arguments.of(new OrderRequest("", 1L, "Ana", null, null,
                        OrderStatus.OPEN, true, PRICE, null, null), List.of("orderNumber")),
                Arguments.of(new OrderRequest(" ", 1L, "Ana", null, null,
                        OrderStatus.OPEN, true, PRICE, null, null), List.of("orderNumber")),
                Arguments.of(new OrderRequest("a".repeat(256), 1L, "Ana", null, null,
                        OrderStatus.OPEN, true, PRICE, null, null), List.of("orderNumber")),
                Arguments.of(new OrderItemRequest(null, null, null, null, null, null, null),
                        List.of("orderId", "itemId", "quantity", "sizeId", "unitPrice")),
                Arguments.of(new OrderItemRequest(0L, -1L, 0, 0L, List.of(0L),
                        new BigDecimal("-1.00"), "a".repeat(256)),
                        List.of("orderId", "itemId", "quantity", "sizeId", "attributeIds",
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
                new CategoryResponse(1L, "FOOD", NAMES),
                new AllergyResponse(8L, "NUTS", NAMES),
                new ItemAttributeResponse(6L, "NO_ONION", NAMES),
                sizeResponse(),
                new RoleResponse(1L, "WAITER", true),
                new FloorResponse(1L, "Piso 1", 1200, 800, 0),
                new ElementTypeResponse(1L, "wall", NAMES, 200, 12, false, "#fff", "#000"),
                new ElementPlanResponse(2L, 1L, "wall", 0, 0, 200, 12, 0, null,
                        new ElementTypeResponse(1L, "wall", NAMES, 200, 12, false, "#fff", "#000")),
                new LayoutResponse(List.of(tableResponse()), List.of()),
                tableResponse(),
                new ItemResponse(4L, "FOOD-1", NAMES, Map.of("ean", "123", "codes", List.of("ABC")),
                        "Roast chicken", PRICE, 1L, true, "chicken.jpg",
                        List.of(new ItemAttributeResponse(6L, "NO_ONION", NAMES)), List.of(sizeResponse())),
                itemResponse(),
                paymentResponse(),
                orderResponse(),
                new OrderResponse(2L, "000001", userResponse(), "Takeaway", List.of(), null,
                        List.of(), List.of(), OrderStatus.OPEN, true, BigDecimal.ZERO, CREATED_AT, 0, null, null));
    }

    private static OrderItemResponse itemResponse() {
        return new OrderItemResponse(5L, 1L, productResponse(), 1, sizeResponse(),
                List.of(new ItemAttributeResponse(6L, "NO_ONION", NAMES)), PRICE, "No salt", CREATED_AT);
    }

    private static ItemResponse productResponse() {
        return new ItemResponse(4L, null, null, null, null, null, 1L, true, null, List.of(), List.of());
    }

    private static TableResponse tableResponse() {
        return new TableResponse(3L, "1", 4, com.ps.restaurant.model.TableStatus.FREE,
                new FloorResponse(1L, "Piso 1", 1200, 800, 0), 0, 0, 100, 80, 0,
                com.ps.restaurant.model.TableShape.RECTANGULAR);
    }

    private static PaymentResponse paymentResponse() {
        return new PaymentResponse(6L, 1L, PRICE, PaymentMethod.CARD, CREATED_AT);
    }

    private static OrderResponse orderResponse() {
        return new OrderResponse(1L, "ORD-2026-001", userResponse(), "Ana", List.of(new AllergyResponse(8L, "NUTS", NAMES)),
                tableResponse(), List.of(itemResponse()), List.of(paymentResponse()),
                OrderStatus.OPEN, false, PRICE, CREATED_AT, 1, null, null);
    }

    private static UserResponse userResponse() {
        return new UserResponse(2L, "ana@example.com", "ana", "Ana", new RoleResponse(1L, "WAITER", true), true);
    }

    private static ItemSizeResponse sizeResponse() {
        return new ItemSizeResponse(7L, "FULL", NAMES);
    }

    private static void assertNotEntity(Type type) {
        if (type instanceof Class<?> clazz) {
            assertThat(clazz.isAnnotationPresent(Entity.class)).as("%s must not be a JPA entity", clazz).isFalse();
        } else if (type instanceof ParameterizedType parameterized) {
            assertNotEntity(parameterized.getRawType());
            Arrays.stream(parameterized.getActualTypeArguments()).forEach(ModelDtoTests::assertNotEntity);
        }
    }

    @Test
    void mapsCatalogEntitiesAndAllItemFields() {
        ItemCategory category = new ItemCategory();
        category.setId(1L);
        category.setCategoryKey("FOOD");
        category.setDisplayName(NAMES);
        assertThat(CategoryResponse.from(category)).isEqualTo(new CategoryResponse(1L, "FOOD", NAMES));

        ItemAttribute attribute = new ItemAttribute();
        attribute.setId(6L);
        attribute.setAttributeKey("NO_ONION");
        attribute.setDisplayName(NAMES);
        ItemSize size = size();

        Item item = new Item();
        item.setId(4L);
        item.setSku("FOOD-1");
        item.setDisplayName(NAMES);
        item.setBarcodes(Map.of("ean", "123", "codes", List.of("ABC")));
        item.setDescription("Roast chicken");
        item.setPrice(PRICE);
        item.setCategory(category);
        item.setAvailable(false);
        item.setImg("chicken.jpg");
        item.setAvailableAttributes(List.of(attribute));
        item.setSizes(List.of(size));

        assertThat(ItemResponse.from(item)).isEqualTo(new ItemResponse(
                4L, "FOOD-1", NAMES, item.getBarcodes(), "Roast chicken", PRICE, 1L, false, "chicken.jpg",
                List.of(new ItemAttributeResponse(6L, "NO_ONION", NAMES)), List.of(sizeResponse())));

        item.setAvailableAttributes(null);
        item.setSizes(null);
        assertThat(ItemResponse.from(item).availableAttributes()).isEmpty();
        assertThat(ItemResponse.from(item).sizes()).isEmpty();
    }

    @Test
    void mapsOrderRelationshipsToDtosWithoutExposingEntities() {
        User waiter = new User();
        waiter.setId(2L);
        waiter.setRole(new com.ps.restaurant.model.Role());
        Allergy allergy = new Allergy();
        allergy.setId(8L);
        allergy.setAllergyKey("NUTS");
        allergy.setDisplayName(NAMES);
        Item item = new Item();
        item.setId(4L);
        ItemCategory category = new ItemCategory();
        category.setId(1L);
        item.setCategory(category);
        Order order = new Order();
        order.setId(1L);
        order.setWaiter(waiter);
        order.setAllergies(List.of(allergy));
        OrderItem line = new OrderItem();
        line.setId(5L);
        line.setOrder(order);
        line.setItem(item);
        line.setSize(size());
        line.setQuantity(1);
        ItemAttribute attribute = new ItemAttribute();
        attribute.setId(6L);
        attribute.setAttributeKey("NO_ONION");
        attribute.setDisplayName(NAMES);
        line.setAttributes(List.of(attribute));
        line.setUnitPrice(PRICE);
        line.setNotes("No salt");
        line.setCreatedAt(CREATED_AT);
        order.setItems(List.of(line));

        OrderResponse response = OrderResponse.from(order);
        assertThat(response.allergies()).containsExactly(new AllergyResponse(8L, "NUTS", NAMES));
        assertThat(response.items()).containsExactly(itemResponse());
        assertThat(response.table()).isNull();
    }

    @Test
    void readsOrderRelationshipsAsIdsAndKeepsOptionalFields() {
        OrderRequest order = JSON.readValue("""
                {"orderNumber":"111111","waiterId":2,"customer":"Ana","allergyIds":[8,9],"togo":true}
                """, OrderRequest.class);
        assertThat(order.allergyIds()).containsExactly(8L, 9L);
        assertThat(order.togo()).isTrue();
        assertThat(order.tableId()).isNull();
        assertThat(order.status()).isNull();
        assertThat(order.total()).isNull();
        assertThat(validator.validate(order)).isEmpty();

        OrderItemRequest line = JSON.readValue("""
                {"orderId":1,"itemId":4,"quantity":1,"sizeId":7,"unitPrice":12.50}
                """, OrderItemRequest.class);
        assertThat(line.itemId()).isEqualTo(4L);
        assertThat(line.sizeId()).isEqualTo(7L);
        assertThat(validator.validate(line)).isEmpty();
    }

    private static ItemSize size() {
        ItemSize size = new ItemSize();
        size.setId(7L);
        size.setSizeKey("FULL");
        size.setDisplayName(NAMES);
        return size;
    }
}
