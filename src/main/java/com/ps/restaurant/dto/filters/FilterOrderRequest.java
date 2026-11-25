package com.ps.restaurant.dto.filters;

import java.time.Instant;

import com.ps.restaurant.model.OrderStatus;

import jakarta.validation.constraints.Positive;

public record FilterOrderRequest(
    @Positive Long id,
    String orderNumber,
    OrderStatus status,
    @Positive Long tableId,
    @Positive Long waiterId,
    Instant fromDate,
    Instant toDate
) {}
