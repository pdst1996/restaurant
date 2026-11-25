package com.ps.restaurant.dto.filters;

import java.time.Instant;

import com.ps.restaurant.model.PaymentMethod;

import jakarta.validation.constraints.Positive;

public record FilterPaymentRequest(
        @Positive Long id,
        @Positive Long orderId,
        PaymentMethod method,
        Instant fromDate,
        Instant toDate) {
}

