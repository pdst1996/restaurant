package com.ps.restaurant.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.ps.restaurant.model.Item;
import com.ps.restaurant.model.Floor;
import com.ps.restaurant.model.ElementType;
import com.ps.restaurant.model.Allergy;
import com.ps.restaurant.model.ItemAttribute;
import com.ps.restaurant.model.ItemCategory;
import com.ps.restaurant.model.ItemSize;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderItem;
import com.ps.restaurant.model.Payment;
import com.ps.restaurant.model.Role;
import com.ps.restaurant.model.Table;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.UserRepository;

import jakarta.persistence.EntityManager;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResourceControllersIntegrationTests {

    private static final JsonMapper JSON = JsonMapper.builder().findAndAddModules().build();
    @Autowired private WebApplicationContext context;
    @Autowired private EntityManager entityManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private DataSource dataSource;

    private MockMvc mvc;
    private Role role;
    private User waiter;
    private Table table;
    private Floor floor;
    private ItemCategory category;
    private ItemAttribute attribute;
    private ItemSize size;
    private Item item;
    private Order order;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        role = new Role();
        role.setName("FIXTURE_ROLE");
        role.setActive(true);
        entityManager.persist(role);
        waiter = new User();
        waiter.setEmail("fixture@example.com");
        waiter.setUser("fixture");
        waiter.setPassword("not-exposed");
        waiter.setFullName("Fixture waiter");
        waiter.setRole(role);
        waiter.setActive(true);
        entityManager.persist(waiter);
        floor = new Floor();
        floor.setName("Fixture floor");
        entityManager.persist(floor);
        ElementType elementType = new ElementType();
        elementType.setType("wall");
        elementType.setLabel(Map.of("es", "Pared", "en", "Wall"));
        elementType.setWidth(200);
        elementType.setHeight(12);
        entityManager.persist(elementType);
        table = new Table();
        table.setFloor(floor);
        table.setTableName("Fixture");
        table.setCapacity(4);
        entityManager.persist(table);
        category = new ItemCategory();
        category.setCategoryKey("FIXTURE_CATEGORY");
        category.setDisplayName(Map.of("es", "Prueba"));
        entityManager.persist(category);
        attribute = new ItemAttribute();
        attribute.setAttributeKey("FIXTURE_ATTRIBUTE");
        attribute.setDisplayName(Map.of("es", "Sin cebolla"));
        entityManager.persist(attribute);
        size = new ItemSize();
        size.setSizeKey("FIXTURE_SIZE");
        size.setDisplayName(Map.of("es", "Grande"));
        entityManager.persist(size);
        item = new Item();
        item.setSku("FIXTURE_ITEM");
        item.setDisplayName(Map.of("es", "Producto"));
        item.setCategory(category);
        item.setPrice(new BigDecimal("12.50"));
        item.setSizes(List.of(size));
        item.setAvailableAttributes(List.of(attribute));
        entityManager.persist(item);
        order = new Order();
        order.setOrderNumber("FIXTURE_ORDER");
        order.setCustomer("Fixture customer");
        order.setWaiter(waiter);
        entityManager.persist(order);
        entityManager.flush();
    }

    @ParameterizedTest
    @ValueSource(strings = {"table", "category", "allergy", "item-attribute", "item-size", "role", "user", "item", "order-item", "payment", "order", "floor", "element-type", "plan-element"})
    void createsListsFiltersAndCountsEveryResource(String resource) throws Exception {
        String response = mvc.perform(post(path(resource)).contentType(MediaType.APPLICATION_JSON)
                        .content(body(resource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();
        long id = JSON.readTree(response).get("id").asLong();
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get(path(resource)).param("id", Long.toString(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get(path(resource) + "/count").param("id", Long.toString(id)))
                .andExpect(status().isOk()).andExpect(jsonPath("$").value(1));
        for (String suffix : List.of("", "/count")) {
            MockHttpServletRequestBuilder query = get(path(resource) + suffix).param("id", Long.toString(id));
            filters(resource).forEach(query::param);
            var result = mvc.perform(query).andExpect(status().isOk());
            if (suffix.isEmpty()) {
                result.andExpect(jsonPath("$.length()").value(1));
            } else {
                result.andExpect(jsonPath("$").value(1));
            }
        }
        for (var filter : filters(resource).entrySet()) {
            String mismatch = switch (filter.getKey()) {
                case "active", "available", "round" -> Boolean.toString(!Boolean.parseBoolean(filter.getValue()));
                case "shape" -> "RECTANGULAR";
                case "status" -> "OCCUPIED";
                case "method" -> "CASH";
                case "quantity", "capacity" -> "9999";
                default -> filter.getKey().endsWith("Id") ? "9223372036854775807" : "MISSING_VALUE";
            };
            mvc.perform(get(path(resource)).param("id", Long.toString(id)).param(filter.getKey(), mismatch))
                    .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
            mvc.perform(get(path(resource) + "/count").param("id", Long.toString(id)).param(filter.getKey(), mismatch))
                    .andExpect(status().isOk()).andExpect(jsonPath("$").value(0));
        }
        mvc.perform(get(path(resource)).param("id", "9223372036854775807"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(get(path(resource) + "/count").param("id", "9223372036854775807"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").value(0));
        String listing = mvc.perform(get(path(resource)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(listing).doesNotContain("\"password\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"table", "category", "allergy", "item-attribute", "item-size", "role", "user", "item", "order-item", "payment", "order", "floor", "element-type", "plan-element"})
    void rejectsInvalidIdsAndBodies(String resource) throws Exception {
        mvc.perform(get(path(resource)).param("id", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get(path(resource) + "/count").param("id", "invalid")).andExpect(status().isBadRequest());
        mvc.perform(post(path(resource)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"table", "category", "allergy", "item-attribute", "item-size", "role", "item", "user", "order", "element-type"})
    void rejectsDuplicateUniqueFields(String resource) throws Exception {
        mvc.perform(post(path(resource)).contentType(MediaType.APPLICATION_JSON).content(body(resource)))
                .andExpect(status().isOk());
        mvc.perform(post(path(resource)).contentType(MediaType.APPLICATION_JSON).content(body(resource)))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(strings = {"item", "user", "order-item", "payment", "order", "table", "plan-element"})
    void rejectsMissingReferences(String resource) throws Exception {
        String invalid = switch (resource) {
            case "item" -> body(resource).replace("\"categoryId\":" + category.getId(), "\"categoryId\":9223372036854775807");
            case "user" -> body(resource).replace("\"id\":" + role.getId(), "\"id\":9223372036854775807");
            case "order" -> body(resource).replace("\"waiterId\":" + waiter.getId(), "\"waiterId\":9223372036854775807");
            case "table", "plan-element" -> body(resource).replace("\"floorId\":" + floor.getId(), "\"floorId\":9223372036854775807");
            default -> body(resource).replace("\"orderId\":" + order.getId(), "\"orderId\":9223372036854775807");
        };
        mvc.perform(post(path(resource)).contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isNotFound());
    }

    @Test
    void createsOrderWithOnlyOrderNumberAndWaiterId() throws Exception {
        String response = mvc.perform(post(path("order")).contentType(MediaType.APPLICATION_JSON).content(
                        "{\"orderNumber\":\"SEED-ORD-232344\",\"waiterId\":" + waiter.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("SEED-ORD-232344"))
                .andExpect(jsonPath("$.user.id").value(waiter.getId()))
                .andExpect(jsonPath("$.togo").value(false))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.payments").isEmpty())
                .andReturn().getResponse().getContentAsString();

        long id = JSON.readTree(response).get("id").asLong();
        entityManager.flush();
        entityManager.clear();

        Order created = entityManager.find(Order.class, id);
        assertThat(created.getOrderNumber()).isEqualTo("SEED-ORD-232344");
        assertThat(created.getWaiter().getId()).isEqualTo(waiter.getId());
        assertThat(created.isTogo()).isFalse();
        assertThat(created.getTable()).isNull();
    }

    @Test
    void persistsItemRelationshipsAndOptionalFields() throws Exception {
        String response = mvc.perform(post(path("item")).contentType(MediaType.APPLICATION_JSON).content(body("item")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.categoryId").value(category.getId()))
                .andExpect(jsonPath("$.availableAttributes.length()").value(1))
                .andExpect(jsonPath("$.sizes.length()").value(1))
                .andExpect(jsonPath("$.barcodes.ean").value("750001"))
                .andExpect(jsonPath("$.img").value("menu.jpg"))
                .andReturn().getResponse().getContentAsString();
        long id = JSON.readTree(response).get("id").asLong();
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get(path("item")).param("id", Long.toString(id)).param("available", "false")
                        .param("sku", "NEW_ITEM").param("categoryId", category.getId().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(path("item")).param("id", Long.toString(id)).param("available", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        mvc.perform(get(path("item") + "/count").param("available", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").value(1));

        // The JSONB function is PostgreSQL-specific; the same test suite also runs on H2.
        try (var connection = dataSource.getConnection()) {
            if ("PostgreSQL".equals(connection.getMetaData().getDatabaseProductName())) {
                mvc.perform(get(path("item")).param("barcode", "750001"))
                        .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
                mvc.perform(get(path("item") + "/count").param("barcode", "750001"))
                        .andExpect(status().isOk()).andExpect(jsonPath("$").value(1));
                mvc.perform(get(path("item")).param("barcode", "NOT_A_BARCODE"))
                        .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
                mvc.perform(get(path("item") + "/count").param("barcode", "NOT_A_BARCODE"))
                        .andExpect(status().isOk()).andExpect(jsonPath("$").value(0));
            }
        }

        mvc.perform(post(path("item")).contentType(MediaType.APPLICATION_JSON).content(
                        "{\"sku\":\"MINIMAL\",\"displayName\":{\"es\":\"Producto\"},\"available\":false,\"price\":0,\"categoryId\":" + category.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sizes").isEmpty())
                .andExpect(jsonPath("$.availableAttributes").isEmpty());
    }

    @Test
    void hashesPasswordsAndUsesThePersistedRole() throws Exception {
        mvc.perform(post(path("user")).contentType(MediaType.APPLICATION_JSON).content(body("user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role.name").value("FIXTURE_ROLE"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());
        entityManager.flush();
        User saved = userRepository.findByUser("newuser").orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", saved.getPassword())).isTrue();
        mvc.perform(get(path("user")).param("user", "newuser").param("email", "new@example.com")
                        .param("fullName", "New User").param("roleId", role.getId().toString()).param("active", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(path("user")).param("active", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void registersUsingOnlyThePersistedRoleAndValidatesTheRoleReference() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body("user")))
                .andExpect(status().isCreated());
        User saved = userRepository.findByUser("newuser").orElseThrow();
        assertThat(saved.getRole().getName()).isEqualTo("FIXTURE_ROLE");
        assertThat(passwordEncoder.matches("secret123", saved.getPassword())).isTrue();
        String missingRole = body("user").replace("newuser", "otheruser").replace("new@example.com", "other@example.com")
                .replace("\"id\":" + role.getId(), "\"id\":9223372036854775807");
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(missingRole))
                .andExpect(status().isNotFound());
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body("user").replace("\"id\":" + role.getId(), "\"id\":0")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void preservesOrderDefaultsAndAllEditableFields() throws Exception {
        mvc.perform(post(path("order")).contentType(MediaType.APPLICATION_JSON).content(body("order")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.table").doesNotExist())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.allergies").isEmpty())
                .andExpect(jsonPath("$.notes").value("Takeaway"))
                .andExpect(jsonPath("$.otherAllergies").value("Kiwi"));
    }

    @Test
    void createsOpenOrderWithTableAllergiesAndIgnoresClientTotal() throws Exception {
        Allergy allergy = new Allergy();
        allergy.setAllergyKey("NUTS");
        allergy.setDisplayName(Map.of("es", "Nueces"));
        entityManager.persist(allergy);
        String request = body("order").replace("\"togo\":true",
                "\"togo\":false,\"status\":\"OPEN\",\"total\":25.00,\"tableId\":" + table.getId()
                        + ",\"allergyIds\":[" + allergy.getId() + "," + allergy.getId() + "]");
        mvc.perform(post(path("order")).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.table.id").value(table.getId()))
                .andExpect(jsonPath("$.user.id").value(waiter.getId()))
                .andExpect(jsonPath("$.allergies.length()").value(1))
                .andExpect(jsonPath("$.allergies[0].allergyKey").value("NUTS"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.total").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"attributeIds", "sizeIds"})
    void rejectsMissingItemCatalogReferences(String field) throws Exception {
        String request = body("item").replaceAll("\"" + field + "\":\\[[^\\]]*\\]",
                "\"" + field + "\":[9223372036854775807]");
        mvc.perform(post(path("item")).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"itemId", "sizeId"})
    void rejectsMissingOrderItemReferences(String field) throws Exception {
        String request = body("order-item").replaceAll("\"" + field + "\":\\d+",
                "\"" + field + "\":9223372036854775807");
        mvc.perform(post(path("order-item")).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void copiesOrderItemAttributesAndNotes() throws Exception {
        String request = body("order-item").replace("\"quantity\":2",
                "\"quantity\":2,\"attributeIds\":[" + attribute.getId() + "," + attribute.getId() + "]");
        String response = mvc.perform(post(path("order-item")).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.unitPrice").value(12.50))
                .andExpect(jsonPath("$.notes").value("Sin sal"))
                .andExpect(jsonPath("$.size.id").value(size.getId()))
                .andExpect(jsonPath("$.attributes.length()").value(1))
                .andExpect(jsonPath("$.attributes[0].id").value(attribute.getId()))
                .andReturn().getResponse().getContentAsString();
        entityManager.flush();
        entityManager.clear();
        OrderItem saved = entityManager.find(OrderItem.class, JSON.readTree(response).get("id").asLong());
        assertThat(saved.getAttributes()).extracting(ItemAttribute::getId).containsExactly(attribute.getId());
        assertThat(saved.getNotes()).isEqualTo("Sin sal");
    }

    @Test
    void rejectsUnknownOrderItemAttributes() throws Exception {
        String request = body("order-item").replace("\"quantity\":2", "\"quantity\":2,\"attributeIds\":[9223372036854775807]");
        mvc.perform(post(path("order-item")).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletesOrderItemsWithoutRecreatingThemFromTheParentCollection() throws Exception {
        String created = mvc.perform(post(path("order-item")).contentType(MediaType.APPLICATION_JSON).content(body("order-item")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long id = JSON.readTree(created).get("id").asLong();
        mvc.perform(delete(path("order-item") + "/" + id)).andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(OrderItem.class, id)).isNull();
        assertThat(entityManager.find(Order.class, order.getId()).getItems()).isEmpty();
    }

    @Test
    void updatesOrdersWithoutNullingStatusOrTotalAndReturns404ForMissingOrders() throws Exception {
        mvc.perform(put(path("order") + "/" + order.getId()).contentType(MediaType.APPLICATION_JSON).content(body("order")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.total").value(0));
        entityManager.flush();
        mvc.perform(put(path("order") + "/9223372036854775807").contentType(MediaType.APPLICATION_JSON).content(body("order")))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"payment", "order-item", "order"})
    void validatesDateRangesForListAndCount(String resource) throws Exception {
        for (String suffix : List.of("", "/count")) {
            mvc.perform(get(path(resource) + suffix)
                            .param("fromDate", "2026-10-30T12:00:00Z")
                            .param("toDate", "2026-10-29T12:00:00Z"))
                    .andExpect(status().isBadRequest());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"payment", "order-item"})
    void filtersByInclusiveAndOpenEndedDates(String resource) throws Exception {
        String response = mvc.perform(post(path(resource)).contentType(MediaType.APPLICATION_JSON).content(body(resource)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long id = JSON.readTree(response).get("id").asLong();
        entityManager.flush();
        entityManager.clear();
        Instant createdAt = resource.equals("payment")
                ? entityManager.find(Payment.class, id).getCreatedAt()
                : entityManager.find(OrderItem.class, id).getCreatedAt();
        for (String suffix : List.of("", "/count")) {
            for (Map<String, String> range : List.of(
                    Map.of("fromDate", createdAt.toString(), "toDate", createdAt.toString()),
                    Map.of("fromDate", createdAt.toString()),
                    Map.of("toDate", createdAt.toString()))) {
                MockHttpServletRequestBuilder query = get(path(resource) + suffix).param("id", Long.toString(id));
                range.forEach(query::param);
                var result = mvc.perform(query).andExpect(status().isOk());
                result.andExpect(jsonPath(suffix.isEmpty() ? "$.length()" : "$").value(1));
            }
            var result = mvc.perform(get(path(resource) + suffix).param("id", Long.toString(id))
                            .param("fromDate", createdAt.plusSeconds(1).toString()))
                    .andExpect(status().isOk());
            result.andExpect(jsonPath(suffix.isEmpty() ? "$.length()" : "$").value(0));
        }
    }

    private Map<String, String> filters(String resource) {
        return switch (resource) {
            case "table" -> Map.of("tableName", "New table", "capacity", "6", "floorId", floor.getId().toString(),
                    "shape", "CIRCULAR", "status", "FREE");
            case "floor" -> Map.of("name", "New floor");
            case "element-type" -> Map.of("type", "plant", "round", "false");
            case "plan-element" -> Map.of("type", "wall", "label", "New wall", "floorId", floor.getId().toString());
            case "category" -> Map.of("categoryKey", "NEW_CATEGORY");
            case "allergy" -> Map.of("allergyKey", "NEW_ALLERGY");
            case "item-attribute" -> Map.of("attributeKey", "NEW_ATTRIBUTE");
            case "item-size" -> Map.of("sizeKey", "NEW_SIZE");
            case "role" -> Map.of("name", "NEW_ROLE", "active", "false");
            case "user" -> Map.of("email", "new@example.com", "user", "newuser", "fullName", "New User",
                    "roleId", role.getId().toString(), "active", "true");
            case "item" -> Map.of("sku", "NEW_ITEM", "categoryId", category.getId().toString(), "available", "false");
            case "payment" -> Map.of("orderId", order.getId().toString(), "method", "CARD");
            case "order-item" -> Map.of("orderId", order.getId().toString(), "itemId", item.getId().toString(),
                    "sizeId", size.getId().toString(), "quantity", "2");
            case "order" -> Map.of("orderNumber", "NEW_ORDER", "waiterId", waiter.getId().toString());
            default -> throw new IllegalArgumentException(resource);
        };
    }

    private String path(String resource) {
        return switch (resource) {
            case "category" -> "/category/categories";
            case "allergy" -> "/allergy/allergies";
            default -> "/" + resource + "/" + resource + "s";
        };
    }

    private String body(String resource) {
        return switch (resource) {
            case "table" -> "{\"tableName\":\"New table\",\"capacity\":6,\"floorId\":" + floor.getId()
                    + ",\"x\":0,\"y\":0,\"width\":150,\"height\":90,\"rotation\":45,\"shape\":\"CIRCULAR\"}";
            case "floor" -> "{\"name\":\"New floor\"}";
            case "element-type" -> "{\"type\":\"plant\",\"label\":{\"es\":\"Planta\"},\"width\":30,\"height\":30,\"round\":false}";
            case "plan-element" -> "{\"floorId\":" + floor.getId()
                    + ",\"type\":\"wall\",\"x\":0,\"y\":0,\"width\":200,\"height\":12,\"label\":\"New wall\"}";
            case "category" -> "{\"categoryKey\":\"NEW_CATEGORY\",\"displayName\":{\"es\":\"Nuevo\"}}";
            case "allergy" -> "{\"allergyKey\":\"NEW_ALLERGY\",\"displayName\":{\"es\":\"Nueces\"}}";
            case "item-attribute" -> "{\"attributeKey\":\"NEW_ATTRIBUTE\",\"displayName\":{\"es\":\"Sin sal\"}}";
            case "item-size" -> "{\"sizeKey\":\"NEW_SIZE\",\"displayName\":{\"es\":\"Chico\"}}";
            case "role" -> "{\"name\":\"NEW_ROLE\",\"active\":false}";
            case "user" -> "{\"email\":\"new@example.com\",\"password\":\"secret123\",\"user\":\"newuser\","
                    + "\"fullName\":\"New User\",\"role\":{\"id\":" + role.getId() + ",\"name\":\"IGNORED\"}}";
            case "item" -> "{\"sku\":\"NEW_ITEM\",\"displayName\":{\"es\":\"Nuevo\"},\"barcodes\":{\"ean\":\"750001\"},"
                    + "\"description\":\"Description\",\"img\":\"menu.jpg\",\"price\":12.50,\"categoryId\":" + category.getId()
                    + ",\"available\":false,\"attributeIds\":[" + attribute.getId() + "," + attribute.getId()
                    + "],\"sizeIds\":[" + size.getId() + "]}";
            case "order-item" -> "{\"orderId\":" + order.getId() + ",\"itemId\":" + item.getId()
                    + ",\"sizeId\":" + size.getId() + ",\"quantity\":2,\"unitPrice\":12.50,\"notes\":\"Sin sal\"}";
            case "payment" -> "{\"orderId\":" + order.getId() + ",\"amount\":25.00,\"method\":\"CARD\"}";
            case "order" -> "{\"orderNumber\":\"NEW_ORDER\",\"waiterId\":" + waiter.getId()
                    + ",\"customer\":\"Ana\",\"togo\":true,\"notes\":\"Takeaway\",\"otherAllergies\":\"Kiwi\"}";
            default -> throw new IllegalArgumentException(resource);
        };
    }
}

