package com.ps.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ElementPlanRequest(
        @NotNull @Positive Long floorId,
        @NotBlank @Size(max = 255) String type,
        @NotNull @PositiveOrZero Integer x,
        @NotNull @PositiveOrZero Integer y,
        @NotNull @Positive Integer width,
        @NotNull @Positive Integer height,
        Integer rotation,
        @Size(max = 255) String label) {
}
