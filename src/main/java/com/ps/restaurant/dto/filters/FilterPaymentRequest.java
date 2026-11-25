package com.ps.restaurant.dto.filters;

import java.time.Instant;

import com.ps.restaurant.model.PaymentMethod;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Max;

public record FilterPaymentRequest(
        @Positive Long id,
        @Positive Long orderId,
        PaymentMethod method,
        Instant fromDate,
        Instant toDate,
        @PositiveOrZero Integer page,
        @Positive @Max(100) Integer size) {
    public FilterPaymentRequest(Long id, Long orderId, PaymentMethod method, Instant fromDate, Instant toDate) {
        this(id, orderId, method, fromDate, toDate, null, null);
    }
}

