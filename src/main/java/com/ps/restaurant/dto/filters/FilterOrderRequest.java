package com.ps.restaurant.dto.filters;

import java.time.Instant;

import com.ps.restaurant.model.OrderStatus;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Max;

public record FilterOrderRequest(
    @Positive Long id,
    String orderNumber,
    OrderStatus status,
    @Positive Long tableId,
    @Positive Long waiterId,
    Instant fromDate,
    Instant toDate,
    @PositiveOrZero Integer page,
    @Positive @Max(100) Integer size
) {
    public FilterOrderRequest(Long id, String orderNumber, OrderStatus status, Long tableId, Long waiterId,
            Instant fromDate, Instant toDate) {
        this(id, orderNumber, status, tableId, waiterId, fromDate, toDate, null, null);
    }
}
