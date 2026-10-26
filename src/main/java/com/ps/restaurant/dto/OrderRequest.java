package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.ps.restaurant.model.OrderStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Holds order fields; items and payments use their own requests, and tableId is optional. */
public record OrderRequest(
        @NotBlank @Size(max = 255) UUID orderNumber,
        @NotNull @Positive Long waiterId,
        @NotBlank @Size(max = 255) String customer,
        List<@NotBlank String> allergies,
        @Positive Long tableId,
        @NotNull OrderStatus status,
        boolean togo,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal total) {
}
