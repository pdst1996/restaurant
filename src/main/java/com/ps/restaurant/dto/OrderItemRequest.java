package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.util.List;

import com.ps.restaurant.model.OrderItemStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Holds an order item with identifiers for its order, menu item, and size. */
public record OrderItemRequest(
        @NotNull @Positive Long orderId,
        @NotNull @Positive Long itemId,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive Long sizeId,
        List<@NotNull @Positive Long> attributeIds,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitPrice,
        @Size(max = 255) String notes,
        OrderItemStatus status) {
}
