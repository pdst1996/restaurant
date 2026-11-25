package com.ps.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderItem;
import com.ps.restaurant.model.Payment;
import com.ps.restaurant.model.PaymentStatus;

class OrderAmountsTests {
    @Test
    void roundsTaxSeparatelyAndExcludesTipsAndUnsettledPaymentsFromPaidPrincipal() {
        var amounts = new OrderAmounts(new BigDecimal("0.16"));
        var order = new Order();
        amounts.initialize(order);
        var item = new OrderItem();
        item.setUnitPrice(new BigDecimal("33.33"));
        item.setQuantity(3);
        order.getItems().add(item);
        add(order, PaymentStatus.COMPLETED, "30.00", "5.00");
        add(order, PaymentStatus.PENDING, "20.00", "1.00");
        add(order, PaymentStatus.FAILED, "40.00", "2.00");
        add(order, PaymentStatus.REFUNDED, "50.00", "10.00");
        var balance = amounts.summarize(order);
        assertThat(balance.subtotal()).isEqualByComparingTo("99.99");
        assertThat(balance.tax()).isEqualByComparingTo("16.00");
        assertThat(balance.total()).isEqualByComparingTo("115.99");
        assertThat(balance.paid()).isEqualByComparingTo("30.00");
        assertThat(balance.pending()).isEqualByComparingTo("20.00");
        assertThat(balance.balance()).isEqualByComparingTo("85.99");
        assertThat(balance.tip()).isEqualByComparingTo("5.00");
    }

    private void add(Order order, PaymentStatus status, String amount, String tip) {
        var payment = new Payment();
        payment.setStatus(status);
        payment.setAmount(new BigDecimal(amount));
        payment.setTip(new BigDecimal(tip));
        order.getPayments().add(payment);
    }
}
