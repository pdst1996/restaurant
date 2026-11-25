package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.ps.restaurant.model.OrderItem;
import com.ps.restaurant.model.OrderItemStatus;

/** Exposes an order item without serializing its parent order or product entity. */
public record OrderItemResponse(
        Long id,
        Long orderId,
        ItemResponse item,
        Integer quantity,
        ItemSizeResponse size,
        List<ItemAttributeResponse> attributes,
        BigDecimal unitPrice,
        String notes,
        Instant createdAt,
        OrderItemStatus status) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getOrder().getId(),
                ItemResponse.from(item.getItem()),
                item.getQuantity(),
                ItemSizeResponse.from(item.getSize()),
                item.getAttributes() == null ? List.of()
                        : item.getAttributes().stream().map(ItemAttributeResponse::from).toList(),
                item.getUnitPrice(),
                item.getNotes(),
                item.getCreatedAt(),
        item.getStatus());
    }
}
