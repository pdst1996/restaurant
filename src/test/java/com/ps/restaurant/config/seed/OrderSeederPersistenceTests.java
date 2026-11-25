package com.ps.restaurant.config.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.model.Item;
import com.ps.restaurant.model.Allergy;
import com.ps.restaurant.model.ItemCategory;
import com.ps.restaurant.model.ItemSize;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderStatus;
import com.ps.restaurant.model.Payment;
import com.ps.restaurant.model.Role;
import com.ps.restaurant.model.Table;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.ItemsRepository;
import com.ps.restaurant.repository.AllergyRepository;
import com.ps.restaurant.repository.OrderRepository;
import com.ps.restaurant.repository.TableRepository;
import com.ps.restaurant.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest(properties = "app.seed.enabled=false")
@ActiveProfiles("test")
@Transactional
class OrderSeederPersistenceTests {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TableRepository tableRepository;
    @Autowired
    private ItemsRepository itemsRepository;
    @Autowired
    private AllergyRepository allergyRepository;

    @Test
    void persistsItemsAndPaymentsByCascadeAndDoesNotDuplicateThemOnRerun() {
        persistDependencies();
        OrderSeeder seeder = new OrderSeeder(orderRepository, userRepository, tableRepository, itemsRepository, allergyRepository);

        seeder.run();
        entityManager.flush();
        entityManager.clear();

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(20);
        assertThat(orders).filteredOn(order -> order.getPayments().size() == 1).hasSize(10);
        assertThat(orders).filteredOn(order -> order.getPayments().size() == 2).hasSize(5);
        assertThat(orders).filteredOn(order -> order.getPayments().size() == 3).hasSize(5);
        assertThat(orders).allSatisfy(order -> {
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getWaiter().getUser()).isEqualTo("poncho");
            if (order.isTogo()) {
                assertThat(order.getAllergies()).isEmpty();
            } else {
                assertThat(order.getAllergies()).extracting(Allergy::getAllergyKey).containsExactly("nuts");
            }
            assertThat(order.getItems()).isNotEmpty().allSatisfy(item -> {
                assertThat(item.getId()).isPositive();
                assertThat(item.getOrder()).isSameAs(order);
                assertThat(item.getSize().getSizeKey()).isEqualTo("FULL");
                assertThat(item.getItem().getSku()).isEqualTo("SEED-TEST-ITEM");
            });
            assertThat(order.getPayments()).allSatisfy(payment -> {
                assertThat(payment.getId()).isPositive();
                assertThat(payment.getOrder()).isSameAs(order);
            });
            assertThat(order.getPayments().stream().map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo(order.getTotal());
        });

        seeder.run();
        entityManager.flush();
        entityManager.clear();

        assertThat(orderRepository.count()).isEqualTo(20);
        assertThat(entityManager.createQuery("select count(p) from Payment p", Long.class).getSingleResult())
                .isEqualTo(35);
        assertThat(entityManager.createQuery("select count(i) from OrderItem i", Long.class).getSingleResult())
                .isEqualTo(39);
    }

    private void persistDependencies() {
        Allergy allergy = new Allergy();
        allergy.setAllergyKey("nuts");
        allergy.setDisplayName(Map.of("es", "Nueces", "en", "Nuts"));
        entityManager.persist(allergy);

        Role role = new Role();
        role.setName("fixture-waiter");
        entityManager.persist(role);
        User waiter = new User();
        waiter.setUser("poncho");
        waiter.setFullName("Mesero de prueba");
        waiter.setEmail("seed-test@example.com");
        waiter.setPassword("test-only");
        waiter.setRole(role);
        entityManager.persist(waiter);

        Table table = new Table();
        com.ps.restaurant.model.Floor floor = new com.ps.restaurant.model.Floor();
        floor.setName("Piso 1");
        entityManager.persist(floor);
        table.setFloor(floor);
        table.setTableName("1");
        table.setCapacity(4);
        entityManager.persist(table);

        ItemCategory category = new ItemCategory();
        category.setCategoryKey("SEED-TEST");
        category.setDisplayName(Map.of("es", "Pruebas", "en", "Tests"));
        entityManager.persist(category);
        ItemSize size = new ItemSize();
        size.setSizeKey("FULL");
        size.setDisplayName(Map.of("es", "Orden", "en", "Full"));
        entityManager.persist(size);
        Item item = new Item();
        item.setSku("SEED-TEST-ITEM");
        item.setDisplayName(Map.of("es", "Producto de prueba", "en", "Test item"));
        item.setPrice(new BigDecimal("10.01"));
        item.setCategory(category);
        item.setSizes(List.of(size));
        entityManager.persist(item);
        entityManager.flush();
    }
}
