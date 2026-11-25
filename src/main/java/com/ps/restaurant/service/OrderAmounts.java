package com.ps.restaurant.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ps.restaurant.dto.OrderBalanceResponse;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.Payment;
import com.ps.restaurant.model.PaymentStatus;

@Component
public class OrderAmounts {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private final BigDecimal defaultTax;

    public OrderAmounts(@Value("${app.orders.tax-rate:0}") BigDecimal defaultTax) {
        if (defaultTax.signum() < 0 || defaultTax.compareTo(BigDecimal.ONE) > 0 || defaultTax.stripTrailingZeros().scale() > 4) {
            throw new IllegalArgumentException("Order tax rate must be 0–1, with up to four decimals");
        }
        this.defaultTax = defaultTax;
    }

    void initialize(Order order) {
        order.setTaxRate(defaultTax);
        order.setTotal(ZERO);
    }

    OrderBalanceResponse summarize(Order order) { return summarize(order, true); }

    private OrderBalanceResponse summarize(Order order, boolean legacyFallback) {
        BigDecimal subtotal = order.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(ZERO, BigDecimal::add);
        // Keep a stored legacy total if an imported order has no surviving item rows.
        if (legacyFallback && order.getTaxRate() == null && order.getItems().isEmpty() && order.getTotal() != null) {
            subtotal = order.getTotal();
        }
        BigDecimal tax = subtotal.multiply(Objects.requireNonNullElse(order.getTaxRate(), ZERO)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);
        BigDecimal paid = amount(order, PaymentStatus.COMPLETED);
        BigDecimal pending = amount(order, PaymentStatus.PENDING);
        BigDecimal tip = order.getPayments().stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getTip).reduce(ZERO, BigDecimal::add);
        return new OrderBalanceResponse(order.getId(), "MXN", subtotal, tax, total, paid, pending,
                total.subtract(paid).max(ZERO), tip);
    }

    OrderBalanceResponse recalculate(Order order) { return summarize(order, false); }

    private BigDecimal amount(Order order, PaymentStatus status) {
        return order.getPayments().stream().filter(p -> p.getStatus() == status)
                .map(Payment::getAmount).reduce(ZERO, BigDecimal::add);
    }
}
