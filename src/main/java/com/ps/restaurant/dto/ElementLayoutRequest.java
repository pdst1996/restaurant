package com.ps.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** A null id creates an element; otherwise the existing element is updated. */
public record ElementLayoutRequest(@Positive Long id, @NotNull @Valid ElementPlanRequest element) {
}
