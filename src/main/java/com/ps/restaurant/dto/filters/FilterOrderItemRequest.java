package com.ps.restaurant.dto.filters;

import java.time.Instant;

import jakarta.validation.constraints.Positive;

public record FilterOrderItemRequest(
        @Positive Long id,
        @Positive Long orderId,
        @Positive Long itemId,
        @Positive Long sizeId,
        @Positive Integer quantity,
        Instant fromDate,
        Instant toDate) {
}

