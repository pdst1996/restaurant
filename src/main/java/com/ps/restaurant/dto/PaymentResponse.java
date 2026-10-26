package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.ps.restaurant.model.PaymentMethod;

/** Exposes payment details and its order identifier without a parent entity reference. */
public record PaymentResponse(Long id, Long orderId, BigDecimal amount, PaymentMethod method, Instant createdAt) {
}
