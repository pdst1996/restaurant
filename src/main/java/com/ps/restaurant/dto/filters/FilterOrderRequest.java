package com.ps.restaurant.dto.filters;

import java.time.Instant;
import java.util.UUID;

import com.ps.restaurant.model.OrderStatus;

import jakarta.validation.constraints.Positive;

public record FilterOrderRequest(
    @Positive Long id,
    UUID orderNumber,
    OrderStatus status,
    @Positive Long tableId,
    Long waiterId,
    Instant fromDate,
    Instant toDate
) {}
