package com.ps.restaurant.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

/** Exercises entity relationships, enum storage, collection updates, and database constraints in transactions. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EntityMappingTests {

    @Autowired
    private EntityManager entityManager;

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
        assertThat(saved.getAllergies()).containsExactly("nuts", "milk", "nuts");
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.OPEN);
        assertThat(saved.getTotal()).isEqualByComparingTo("25.00");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isPositive();
            assertThat(item.getOrder()).isSameAs(saved);
            assertThat(item.getSize()).isEqualTo(ItemSize.WHOLE);
            assertThat(item.getAttributes()).containsExactly("no onion", "extra cheese");
            assertThat(item.getProduct().getCategory().getProducts())
                    .extracting(Product::getId).contains(item.getProduct().getId());
            assertThat(item.getProduct().getOrderItems()).extracting(OrderItem::getId).contains(item.getId());
        });
        assertThat(saved.getPayments()).singleElement().satisfies(payment -> {
            assertThat(payment.getOrder()).isSameAs(saved);
            assertThat(payment.getAmount()).isEqualByComparingTo("25.00");
            assertThat(payment.getMethod()).isEqualTo(PaymentMethod.CARD);
        });
        assertThat(entityManager.createNativeQuery("select size from order_items where order_id = :id", String.class)
                .setParameter("id", saved.getId()).getSingleResult()).isEqualTo("WHOLE");
        assertThat(entityManager.createNativeQuery("select status from orders where id = :id", String.class)
                .setParameter("id", saved.getId()).getSingleResult()).isEqualTo("OPEN");
        assertThat(entityManager.createNativeQuery("select method from payments where order_id = :id", String.class)
                .setParameter("id", saved.getId()).getSingleResult()).isEqualTo("CARD");
    }

    /** Checks array ordering and item orphan removal while ensuring referenced products and payments survive. */
    @Test
    void updatesOrderedCollectionsAndRemovesOrphanedItemsWithoutDeletingProductsOrPayments() {
        Order order = persistOrder();
        Long orderId = order.getId();
        Long itemId = order.getItems().get(0).getId();
        Long productId = order.getItems().get(0).getProduct().getId();
        Long paymentId = order.getPayments().get(0).getId();
        entityManager.clear();

        Order saved = entityManager.find(Order.class, orderId);
        saved.getAllergies().remove(1);
        saved.getAllergies().add("eggs");
        saved.getItems().get(0).getAttributes().clear();
        saved.getItems().get(0).getAttributes().add("no salt");
        entityManager.flush();
        entityManager.clear();

        saved = entityManager.find(Order.class, orderId);
        assertThat(saved.getAllergies()).containsExactly("nuts", "nuts", "eggs");
        assertThat(saved.getItems().get(0).getAttributes()).containsExactly("no salt");
        saved.getItems().clear();
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(OrderItem.class, itemId)).isNull();
        assertThat(entityManager.find(Product.class, productId)).isNotNull();
        assertThat(entityManager.find(Payment.class, paymentId)).isNotNull();
    }

    /** Persists an order without a table and verifies its empty collections and zero-total default after reload. */
    @Test
    void supportsTakeawayOrdersAndEmptyCollections() {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setWaiter(persistWaiter());
        order.setCustomer("Takeaway customer");
        entityManager.persist(order);
        entityManager.flush();
        entityManager.clear();

        Order saved = entityManager.find(Order.class, order.getId());
        assertThat(saved.getTable()).isNull();
        assertThat(saved.getAllergies()).isEmpty();
        assertThat(saved.getItems()).isEmpty();
        assertThat(saved.getPayments()).isEmpty();
        assertThat(saved.getTotal()).isZero();
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
        Category category = new Category();
        category.setName("Food-" + UUID.randomUUID());
        entityManager.persist(category);

        Product product = new Product();
        product.setSku(UUID.randomUUID().toString());
        product.setName("Test product");
        product.setPrice(new BigDecimal("12.50"));
        product.setCategory(category);
        entityManager.persist(product);

        Table table = new Table();
        table.setTableNumber(1);
        table.setCapacity(4);
        entityManager.persist(table);

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setWaiter(persistWaiter());
        order.setCustomer("Test customer");
        order.setTable(table);
        order.getAllergies().addAll(List.of("nuts", "milk", "nuts"));
        order.setTotal(new BigDecimal("25.00"));

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setSize(ItemSize.WHOLE);
        item.getAttributes().addAll(List.of("no onion", "extra cheese"));
        item.setUnitPrice(product.getPrice());
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
