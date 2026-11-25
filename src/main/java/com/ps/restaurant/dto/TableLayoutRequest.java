package com.ps.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** A null id creates a table; otherwise the existing table is updated. */
public record TableLayoutRequest(@Positive Long id, @NotNull @Valid TableRequest table) {
}
