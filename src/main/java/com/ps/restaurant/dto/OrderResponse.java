package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.ps.restaurant.model.Order;
import com.ps.restaurant.model.OrderStatus;

/** Exposes an order and child DTOs without serializing the waiter or parent entity graph. */
public record OrderResponse(
        Long id,
        String orderNumber,
        UserResponse user,
        String customer,
        List<AllergyResponse> allergies,
        TableResponse table,
        List<OrderItemResponse> items,
        List<PaymentResponse> payments,
        OrderStatus status,
        boolean togo,
        BigDecimal total,
        Instant createdAt,
        Integer itemsQty,
        String otherAllergies,
        String notes) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                UserResponse.from(order.getWaiter()),
                order.getCustomer(),
                order.getAllergies().stream().map(AllergyResponse::from).toList(),
                order.getTable() == null ? null : TableResponse.from(order.getTable()),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getPayments().stream().map(PaymentResponse::from).toList(),
                order.getStatus(),
                order.isTogo(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getItems().size(),
                order.getOtherAllergies(),
                order.getNotes()
            );
    }
}
