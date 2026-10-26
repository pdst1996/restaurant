package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.ps.restaurant.model.OrderStatus;

/** Exposes an order and child DTOs without serializing the waiter or parent entity graph. */
public record OrderResponse(
        Long id,
        UUID orderNumber,
        Long waiterId,
        String customer,
        List<String> allergies,
        Long tableId,
        List<OrderItemResponse> items,
        List<PaymentResponse> payments,
        OrderStatus status,
        boolean togo,
        BigDecimal total,
        Instant createdAt) {
}
