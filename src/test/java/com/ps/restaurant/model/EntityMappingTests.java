package com.ps.restaurant.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.dto.filters.FilterOrderRequest;
import com.ps.restaurant.repository.OrderRepository;
import com.ps.restaurant.repository.specification.OrderSpecifications;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

/** Exercises entity relationships, enum storage, collection updates, and database constraints in transactions. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EntityMappingTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @ParameterizedTest
    @ValueSource(strings = {"111111", "000001", "ORD-2026-001", "7f8b2c4d-6e90-4a13-9c57-2d8f0e6b1a34"})
    void persistsAndFindsStringOrderNumbersWithoutChangingThem(String number) {
        Order order = new Order();
        order.setOrderNumber(number);
        order.setWaiter(persistWaiter());
        order.setCustomer("String folio customer");
        entityManager.persist(order);
        entityManager.flush();
        assertThat(entityManager.createNativeQuery("""
                select lower(data_type) from information_schema.columns
                where lower(table_name) = 'orders' and lower(column_name) = 'order_number'
                """, String.class).getSingleResult()).isEqualTo("character varying");
        assertThat(entityManager.createNativeQuery("select order_number from orders where id = :id", String.class)
                .setParameter("id", order.getId()).getSingleResult()).isEqualTo(number);
        entityManager.clear();

        assertThat(entityManager.find(Order.class, order.getId()).getOrderNumber()).isEqualTo(number);
        assertThat(orderRepository.findByOrderNumber(number)).get()
                .extracting(Order::getId).isEqualTo(order.getId());
        FilterOrderRequest filter = new FilterOrderRequest(null, number, null, null, null, null, null);
        assertThat(orderRepository.findAll(OrderSpecifications.withFilters(filter)))
                .extracting(Order::getId).containsExactly(order.getId());
        assertThat(orderRepository.findByOrderNumber("missing-folio")).isEmpty();
    }

    @Test
    void filtersOrdersByIdentifiersStatusAndInclusiveDateRange() {
        Order order = persistOrder();
        // Use the stored timestamp precision when testing inclusive boundaries.
        entityManager.refresh(order);
        FilterOrderRequest matching = new FilterOrderRequest(
                order.getId(), order.getOrderNumber(), order.getStatus(),
                order.getTable().getId(), order.getWaiter().getId(),
                order.getCreatedAt(), order.getCreatedAt());

        assertThat(orderRepository.findAll(OrderSpecifications.withFilters(matching)))
                .extracting(Order::getId).containsExactly(order.getId());

        FilterOrderRequest otherWaiter = new FilterOrderRequest(
                null, null, null, null, -1L, null, null);
        assertThat(orderRepository.findAll(OrderSpecifications.withFilters(otherWaiter))).isEmpty();

        FilterOrderRequest later = new FilterOrderRequest(
                null, null, null, null, null, order.getCreatedAt().plusSeconds(1), null);
        assertThat(orderRepository.findAll(OrderSpecifications.withFilters(later))).isEmpty();
    }

    /** Reloads a complete order graph and checks relationship navigation, defaults, arrays, and textual enum values. */
    @Test
    void persistsAndReloadsAllEntitiesAndStoresEnumsAsText() {
        Order order = persistOrder();
        entityManager.clear();

        Order saved = entityManager.find(Order.class, order.getId());
        assertThat(saved.getWaiter().getId()).isPositive();
        assertThat(saved.getWaiter().getRole().getId()).isPositive();
        assertThat(saved.getWaiter().getRole().getName()).startsWith("WAITER-");
        assertThat(saved.getTable().getOrders()).extracting(Order::getId).contains(saved.getId());
        assertThat(saved.getAllergies()).extracting(Allergy::getAllergyKey)
                .containsExactlyInAnyOrder("nuts", "milk");
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(saved.getTotal()).isEqualByComparingTo("25.00");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isPositive();
            assertThat(item.getOrder()).isSameAs(saved);
            assertThat(item.getSize().getId()).isPositive();
            assertThat(item.getSize().getSizeKey()).isEqualTo("FULL");
            assertThat(item.getSize().getDisplayName()).containsEntry("en", "Full");
            assertThat(item.getAttributes()).extracting(ItemAttribute::getAttributeKey)
                    .containsExactlyInAnyOrder("NO_ONION", "EXTRA_CHEESE");
            assertThat(item.getItem().getCategory().getProducts())
                    .extracting(Item::getId).contains(item.getItem().getId());
            assertThat(item.getItem().getOrderItems()).extracting(OrderItem::getId).contains(item.getId());
        });
        assertThat(saved.getPayments()).singleElement().satisfies(payment -> {
            assertThat(payment.getOrder()).isSameAs(saved);
            assertThat(payment.getAmount()).isEqualByComparingTo("25.00");
            assertThat(payment.getMethod()).isEqualTo(PaymentMethod.CARD);
        });
        assertThat(entityManager.createNativeQuery("select item_size from order_items where order_id = :id", Long.class)
                .setParameter("id", saved.getId()).getSingleResult())
                .isEqualTo(saved.getItems().get(0).getSize().getId());
        assertThat(entityManager.createNativeQuery("select status from orders where id = :id", String.class)
                .setParameter("id", saved.getId()).getSingleResult()).isEqualTo("DRAFT");
        assertThat(entityManager.createNativeQuery("select method from payments where order_id = :id", String.class)
                .setParameter("id", saved.getId()).getSingleResult()).isEqualTo("CARD");
    }

    /** Checks allergy links, attribute ordering, and orphan removal without deleting referenced entities. */
    @Test
    void updatesOrderedCollectionsAndRemovesOrphanedItemsWithoutDeletingProductsOrPayments() {
        Order order = persistOrder();
        Long orderId = order.getId();
        Long itemId = order.getItems().get(0).getId();
        Long productId = order.getItems().get(0).getItem().getId();
        Long paymentId = order.getPayments().get(0).getId();
        entityManager.clear();

        Order saved = entityManager.find(Order.class, orderId);
        saved.getAllergies().removeIf(allergy -> allergy.getAllergyKey().equals("milk"));
        saved.getAllergies().add(persistAllergy("eggs"));
        saved.getItems().get(0).getAttributes().clear();
        saved.getItems().get(0).getAttributes().add(persistAttribute("NO_SALT"));
        entityManager.flush();
        entityManager.clear();

        saved = entityManager.find(Order.class, orderId);
        assertThat(saved.getAllergies()).extracting(Allergy::getAllergyKey)
                .containsExactlyInAnyOrder("nuts", "eggs");
        assertThat(saved.getItems().get(0).getAttributes()).extracting(ItemAttribute::getAttributeKey)
                .containsExactly("NO_SALT");
        saved.getItems().clear();
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(OrderItem.class, itemId)).isNull();
        assertThat(entityManager.find(Item.class, productId)).isNotNull();
        assertThat(entityManager.find(Payment.class, paymentId)).isNotNull();
        assertThat(entityManager.createQuery("select a.allergyKey from Allergy a", String.class).getResultList())
                .contains("nuts", "milk", "eggs");
    }

    /** Persists an order without a table and verifies its empty collections and zero-total default after reload. */
    @Test
    void supportsTakeawayOrdersAndEmptyCollections() {
        Order order = new Order();
        order.setOrderNumber("TAKEAWAY-001");
        order.setWaiter(persistWaiter());
        order.setCustomer("Takeaway customer");
        order.setTogo(true);
        entityManager.persist(order);
        entityManager.flush();
        entityManager.clear();

        Order saved = entityManager.find(Order.class, order.getId());
        assertThat(saved.getTable()).isNull();
        assertThat(saved.isTogo()).isTrue();
        assertThat(saved.getAllergies()).isEmpty();
        assertThat(saved.getItems()).isEmpty();
        assertThat(saved.getPayments()).isEmpty();
        assertThat(saved.getTotal()).isZero();

        FilterOrderRequest noFilters = new FilterOrderRequest(null, null, null, null, null, null, null);
        assertThat(orderRepository.findAll(OrderSpecifications.withFilters(noFilters)))
                .extracting(Order::getId).containsExactly(order.getId());
    }

    /** Attempts to persist a duplicate email and requires the database constraint to raise a persistence error. */
    @Test
    void enforcesUniqueEmailAtDatabaseLevel() {
        User first = persistWaiter();
        User duplicate = new User();
        duplicate.setEmail(first.getEmail());
        duplicate.setUser(UUID.randomUUID().toString());
        duplicate.setPassword("test-password");
        duplicate.setFullName("Duplicate User");
        duplicate.setRole(first.getRole());

        assertThatThrownBy(() -> {
            entityManager.persist(duplicate);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    /** Persists and flushes a complete fixture with a category, product, table, waiter, item, and payment. */
    private Order persistOrder() {
        ItemCategory category = new ItemCategory();
        category.setCategoryKey("FOOD-" + UUID.randomUUID());
        category.setDisplayName(Map.of("en", "Food", "es", "Comida"));
        entityManager.persist(category);

        Item product = new Item();
        product.setSku(UUID.randomUUID().toString());
        product.setDisplayName(Map.of("en", "Chicken", "es", "Pollo"));
        product.setPrice(new BigDecimal("12.50"));
        product.setCategory(category);
        entityManager.persist(product);

        Table table = new Table();
        Floor floor = new Floor();
        floor.setName("Piso 1");
        entityManager.persist(floor);
        table.setFloor(floor);
        table.setTableName("1");
        table.setCapacity(4);
        entityManager.persist(table);

        Order order = new Order();
        order.setOrderNumber("111111");
        order.setWaiter(persistWaiter());
        order.setCustomer("Test customer");
        order.setTable(table);
        order.getAllergies().addAll(List.of(persistAllergy("nuts"), persistAllergy("milk")));
        order.setTotal(new BigDecimal("25.00"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setItem(product);
        item.setQuantity(2);
        ItemSize size = new ItemSize();
        size.setSizeKey("FULL");
        size.setDisplayName(Map.of("en", "Full", "es", "Orden"));
        entityManager.persist(size);
        item.setSize(size);
        item.setUnitPrice(product.getPrice());
        item.getAttributes().addAll(List.of(persistAttribute("NO_ONION"), persistAttribute("EXTRA_CHEESE")));
        order.getItems().add(item);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotal());
        payment.setMethod(PaymentMethod.CARD);
        order.getPayments().add(payment);

        entityManager.persist(order);
        entityManager.flush();
        return order;
    }

    private ItemAttribute persistAttribute(String key) {
        ItemAttribute attribute = new ItemAttribute();
        attribute.setAttributeKey(key);
        attribute.setDisplayName(Map.of("en", key, "es", key));
        entityManager.persist(attribute);
        return attribute;
    }

    private Allergy persistAllergy(String key) {
        Allergy allergy = new Allergy();
        allergy.setAllergyKey(key);
        allergy.setDisplayName(Map.of("en", key, "es", key));
        entityManager.persist(allergy);
        return allergy;
    }

    /** Persists a uniquely identified active waiter and role for use in transactional order tests. */
    private User persistWaiter() {
        String suffix = UUID.randomUUID().toString();
        Role role = new Role();
        role.setName("WAITER-" + suffix);
        role.setActive(true);
        entityManager.persist(role);

        User waiter = new User();
        waiter.setEmail(suffix + "@example.com");
        waiter.setUser(suffix);
        waiter.setPassword("test-password");
        waiter.setFullName("Test Waiter");
        waiter.setRole(role);
        waiter.setActive(true);
        entityManager.persist(waiter);
        return waiter;
    }
}
