package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.ps.restaurant.model.ItemSize;

/** Exposes an order item without serializing its parent order or product entity. */
public record OrderItemResponse(
        Long id,
        Long orderId,
        Long productId,
        Integer quantity,
        ItemSize size,
        List<String> attributes,
        BigDecimal unitPrice,
        String notes,
        Instant createdAt) {
}
