package com.ps.restaurant.config.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.ps.restaurant.model.Item;
import com.ps.restaurant.model.Allergy;
import com.ps.restaurant.model.ItemSize;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderStatus;
import com.ps.restaurant.model.Payment;
import com.ps.restaurant.model.PaymentMethod;
import com.ps.restaurant.model.Table;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.ItemsRepository;
import com.ps.restaurant.repository.AllergyRepository;
import com.ps.restaurant.repository.OrderRepository;
import com.ps.restaurant.repository.TableRepository;
import com.ps.restaurant.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderSeederTests {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TableRepository tableRepository;
    @Mock
    private ItemsRepository itemsRepository;
    @Mock
    private AllergyRepository allergyRepository;

    private OrderSeeder seeder;
    private final Map<String, Order> orders = new HashMap<>();

    @BeforeEach
    void setUp() {
        seeder = new OrderSeeder(orderRepository, userRepository, tableRepository, itemsRepository, allergyRepository);
        when(orderRepository.findByOrderNumber(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(orders.get(invocation.getArgument(0))));
    }

    @Test
    void createsTwentyPaidOrdersWithSingleAndSplitPaymentsAndExactTotals() {
        stubDependencies();
        captureSavedOrders();
        Instant before = Instant.now();

        seeder.run();

        assertThat(orders).hasSize(20).containsKeys("SEED-ORD-001", "SEED-ORD-020");
        assertThat(orders.values()).filteredOn(order -> order.getPayments().size() == 1).hasSize(10);
        assertThat(orders.values()).filteredOn(order -> order.getPayments().size() == 2).hasSize(5);
        assertThat(orders.values()).filteredOn(order -> order.getPayments().size() == 3).hasSize(5);
        assertThat(orders.values()).filteredOn(Order::isTogo).hasSize(5);
        assertThat(orders.values()).allSatisfy(order -> {
            if (order.isTogo()) {
                assertThat(order.getAllergies()).isEmpty();
            } else {
                assertThat(order.getAllergies()).hasSizeBetween(1, 2)
                        .extracting(Allergy::getAllergyKey).containsAnyOf("nuts", "milk");
            }
        });
        assertThat(orders.values().stream().flatMap(order -> order.getPayments().stream()))
                .hasSize(35).extracting(Payment::getMethod)
                .contains(PaymentMethod.CASH, PaymentMethod.CARD, PaymentMethod.TRANSFER);

        assertThat(orders.values()).allSatisfy(order -> {
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getWaiter().getUser()).isEqualTo("poncho");
            assertThat(order.getCustomer()).isNotBlank();
            assertThat(order.getCreatedAt()).isBefore(before);
            assertThat(order.getTable() == null).isEqualTo(order.isTogo());
            assertThat(order.getItems()).hasSizeBetween(1, 3).allSatisfy(item -> {
                assertThat(item.getOrder()).isSameAs(order);
                assertThat(item.getItem().getSizes()).contains(item.getSize());
                assertThat(item.getQuantity()).isPositive();
                assertThat(item.getUnitPrice()).isEqualByComparingTo(item.getItem().getPrice());
            });
            BigDecimal itemTotal = order.getItems().stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paidTotal = order.getPayments().stream().map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(order.getTotal()).isEqualByComparingTo(itemTotal).isEqualByComparingTo(paidTotal);
            assertThat(order.getPayments()).allSatisfy(payment -> {
                assertThat(payment.getOrder()).isSameAs(order);
                assertThat(payment.getAmount()).isPositive();
                assertThat(payment.getAmount().scale()).isEqualTo(2);
                assertThat(payment.getCreatedAt()).isAfter(order.getCreatedAt()).isBefore(before);
            });
        });
        // 30.03 / 2 must keep the remaining cent in the final payment.
        assertThat(orders.get("SEED-ORD-014").getPayments()).extracting(Payment::getAmount)
                .containsExactly(new BigDecimal("15.01"), new BigDecimal("15.02"));
    }

    @Test
    void doesNotDuplicateOrdersOrPaymentsWhenRunAgain() {
        stubDependencies();
        captureSavedOrders();
        seeder.run();
        Map<String, Order> originalOrders = new HashMap<>(orders);

        seeder.run();

        assertThat(orders).containsExactlyInAnyOrderEntriesOf(originalOrders);
        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, org.mockito.Mockito.times(20)).save(saved.capture());
        verify(userRepository).findByUser("poncho");
    }

    @Test
    void preservesExistingOrdersAndOnlyCreatesMissingOnes() {
        stubDependencies();
        captureSavedOrders();
        Order existing = new Order();
        existing.setOrderNumber("SEED-ORD-001");
        existing.setCustomer("Cliente existente");
        orders.put(existing.getOrderNumber(), existing);

        seeder.run();

        assertThat(orders).hasSize(20);
        assertThat(orders.get(existing.getOrderNumber())).isSameAs(existing);
        assertThat(existing.getCustomer()).isEqualTo("Cliente existente");
        verify(orderRepository, never()).save(existing);
        verify(orderRepository, org.mockito.Mockito.times(19)).save(any(Order.class));
    }

    @Test
    void failsClearlyWhenSeedWaiterDoesNotExist() {
        assertThatThrownBy(() -> seeder.run()).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UserSeeder");
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(tableRepository, itemsRepository, allergyRepository);
    }

    @Test
    void failsWithoutTablesOrUsableItems() {
        when(userRepository.findByUser("poncho")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> seeder.run()).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TableSeeder and ItemsSeeder");
        verify(orderRepository, never()).save(any(Order.class));
    }

    private void captureSavedOrders() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            orders.put(order.getOrderNumber(), order);
            return order;
        });
    }

    private void stubDependencies() {
        User waiter = new User();
        waiter.setUser("poncho");
        when(userRepository.findByUser("poncho")).thenReturn(Optional.of(waiter));
        Table table = new Table();
        table.setTableName("1");
        when(tableRepository.findAll(Sort.by("tableName"))).thenReturn(List.of(table));
        ItemSize size = new ItemSize();
        size.setSizeKey("FULL");
        Item item = new Item();
        item.setPrice(new BigDecimal("10.01"));
        item.setSizes(List.of(size));
        when(itemsRepository.findAll(Sort.by("id"))).thenReturn(List.of(item));
        Allergy nuts = new Allergy();
        nuts.setId(1L);
        nuts.setAllergyKey("nuts");
        Allergy milk = new Allergy();
        milk.setId(2L);
        milk.setAllergyKey("milk");
        when(allergyRepository.findAll(Sort.by("id"))).thenReturn(List.of(nuts, milk));
    }
}
