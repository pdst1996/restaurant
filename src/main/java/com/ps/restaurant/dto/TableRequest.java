package com.ps.restaurant.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Holds a table number and optional capacity without accepting related orders. */
public record TableRequest(
        @NotNull @Positive Integer tableNumber,
        @Positive Integer capacity) {
}
