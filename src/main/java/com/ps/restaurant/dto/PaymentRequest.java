package com.ps.restaurant.dto;

import java.math.BigDecimal;

import com.ps.restaurant.model.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Holds a payment amount and method for an existing order. */
public record PaymentRequest(
        @NotNull @Positive Long orderId,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull PaymentMethod method) {
}
