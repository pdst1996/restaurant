package com.ps.restaurant.config.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ps.restaurant.model.Allergy;
import com.ps.restaurant.model.Item;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderItem;
import com.ps.restaurant.model.OrderItemStatus;
import com.ps.restaurant.model.OrderStatus;
import com.ps.restaurant.model.Payment;
import com.ps.restaurant.model.PaymentMethod;
import com.ps.restaurant.model.Table;
import com.ps.restaurant.model.User;
import com.ps.restaurant.repository.AllergyRepository;
import com.ps.restaurant.repository.ItemsRepository;
import com.ps.restaurant.repository.OrderRepository;
import com.ps.restaurant.repository.TableRepository;
import com.ps.restaurant.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds 20 paid orders: ten with one payment, five with two, and five with
 * three.
 */
@Component
@org.springframework.core.annotation.Order(4)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class OrderSeeder implements CommandLineRunner {

    private static final int ORDER_COUNT = 20;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TableRepository tableRepository;
    private final ItemsRepository itemsRepository;
    private final AllergyRepository allergyRepository;

    @Override
    @Transactional
    @SuppressWarnings("null")
    public void run(String... args) {
        createAllergies();
        List<Integer> missingOrders = new ArrayList<>();
        for (int number = 1; number <= ORDER_COUNT; number++) {
            if (orderRepository.findByOrderNumber(orderNumber(number)).isEmpty()) {
                missingOrders.add(number);
            }
        }
        if (missingOrders.isEmpty()) {
            log.debug("Seed: all sample orders already exist");
            return;
        }

        User waiter = userRepository.findByUser("poncho")
                .orElseThrow(() -> new IllegalStateException("Seed: run UserSeeder before OrderSeeder"));
        List<Table> tables = tableRepository.findAll(Sort.by("tableName"));
        List<Item> items = itemsRepository.findAll(Sort.by("id")).stream()
                .filter(Item::isAvailable)
                .filter(item -> item.getPrice() != null && item.getPrice().signum() > 0)
                .filter(item -> item.getSizes() != null && !item.getSizes().isEmpty())
                .toList();
        
        List<Allergy> allergies = allergyRepository.findAll(
            Sort.by("id")
        );
        if (tables.isEmpty() || items.isEmpty()) {
            throw new IllegalStateException("Seed: run TableSeeder and ItemsSeeder before OrderSeeder");
        }

        Instant now = Instant.now();
        for (int number : missingOrders) {
            Order order = createOrder(number, waiter, tables, items, allergies, now);
            orderRepository.save(order);
            log.info("Seed: order {} created with {} payment(s)",
                    order.getOrderNumber(), order.getPayments().size());
        }
    }

    private void createAllergies() {
        createAllergy("MILK", "Leche", "Milk");
        createAllergy("EGGS", "Huevo", "Eggs");
        createAllergy("PEANUTS", "Cacahuates", "Peanuts");
        createAllergy("TREE_NUTS", "Frutos secos", "Tree nuts");
        createAllergy("SOY", "Soya", "Soy");
        createAllergy("WHEAT", "Trigo", "Wheat");
        createAllergy("GLUTEN", "Gluten", "Gluten");
        createAllergy("SHELLFISH", "Mariscos", "Shellfish");
        createAllergy("SESAME", "Sésamo", "Sesame");
        createAllergy("MUSTARD", "Mostaza", "Mustard");
        createAllergy("SULFITES", "Sulfitos", "Sulfites");
    }

    private void createAllergy(
            String allergyKey,
            String spanishName,
            String englishName) {

        if (allergyRepository.existsByAllergyKey(allergyKey)) {
            log.debug("Seed: allergy {} already exists", allergyKey);
            return;
        }

        Allergy allergy = new Allergy();
        allergy.setAllergyKey(allergyKey);
        allergy.setDisplayName(Map.of(
            "es", spanishName,
            "en", englishName
        ));

        allergyRepository.save(allergy);
        log.info("Seed: allergy {} created", allergyKey);
    }

    private Order createOrder(int number, User waiter, List<Table> tables, List<Item> items, List<Allergy> availableAllergies, Instant now) {
        Order order = new Order();
        order.setOrderNumber(orderNumber(number));
        order.setWaiter(waiter);
        order.setCustomer("Cliente de prueba " + number);
        order.setStatus(OrderStatus.PAID);
        order.setTogo(number % 4 == 0);
        order.setAllergies(selectAllergies(number, availableAllergies));
        if (!order.isTogo()) {
            order.setTable(tables.get((number - 1) % tables.size()));
        }
        order.setCreatedAt(now.minus(ORDER_COUNT - number + 1L, ChronoUnit.DAYS));

        BigDecimal total = BigDecimal.ZERO;
        int itemCount = 1 + (number - 1) % 3;
        for (int index = 0; index < itemCount; index++) {
            Item item = items.get((number - 1 + index) % items.size());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setQuantity(1 + (number + index) % 2);
            orderItem.setSize(item.getSizes().get(0));
            orderItem.setUnitPrice(item.getPrice().setScale(2, RoundingMode.HALF_UP));
            orderItem.setCreatedAt(order.getCreatedAt());
            orderItem.setStatus(OrderItemStatus.SERVED);
            order.getItems().add(orderItem);
            total = total.add(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }
        order.setTotal(total);
        addPayments(order, number);
        return order;
    }

    private void addPayments(Order order, int number) {
        int paymentCount = number <= 10 ? 1 : 2 + number % 2;
        BigDecimal installment = order.getTotal().divide(BigDecimal.valueOf(paymentCount), 2, RoundingMode.DOWN);
        BigDecimal remaining = order.getTotal();
        PaymentMethod[] methods = PaymentMethod.values();
        for (int index = 0; index < paymentCount; index++) {
            Payment payment = new Payment();
            payment.setOrder(order);
            // The last payment absorbs rounding so the paid amount always equals the order
            // total.
            payment.setAmount(index == paymentCount - 1 ? remaining : installment);
            payment.setMethod(methods[(number - 1 + index) % methods.length]);
            payment.setCreatedAt(order.getCreatedAt().plus(30L + index, ChronoUnit.MINUTES));
            order.getPayments().add(payment);
            remaining = remaining.subtract(payment.getAmount());
        }
    }

    private String orderNumber(int number) {
        return "SEED-ORD-%03d".formatted(number);
    }

    private List<Allergy> selectAllergies(
            int orderNumber,
            List<Allergy> availableAllergies) {
        if (orderNumber % 4 == 0 || availableAllergies.isEmpty()) {
            return new ArrayList<>();
        }

        List<Allergy> selected = new ArrayList<>(availableAllergies);

        Random random = new Random(orderNumber);
        Collections.shuffle(selected, random);

        int maxAllergies = Math.min(3, selected.size());
        int count = 1 + random.nextInt(maxAllergies);

        return new ArrayList<>(selected.subList(0, count));
    }
}
