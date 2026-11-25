package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.ps.restaurant.model.OrderStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Holds order fields; tableId is optional, and omitted or null togo defaults to false. */
public record OrderRequest(
        @NotBlank @Size(max = 255) String orderNumber,
        @NotNull @Positive Long waiterId,
        @Size(max = 255) String customer,
        @Positive Long tableId,
        List<@NotNull @Positive Long> allergyIds,
        OrderStatus status,
        @JsonSetter(nulls = Nulls.AS_EMPTY) boolean togo,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal total,
        String otherAllergies,
        String notes) {
}
