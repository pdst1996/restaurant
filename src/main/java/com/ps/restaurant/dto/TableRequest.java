package com.ps.restaurant.dto;

import com.ps.restaurant.model.TableShape;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Holds table geometry and an existing floor identifier without accepting related orders. */
public record TableRequest(
        @NotBlank @Size(max = 255) String tableName,
        @Positive Integer capacity,
        @NotNull @Positive Long floorId,
        @PositiveOrZero Integer x,
        @PositiveOrZero Integer y,
        @Positive Integer width,
        @Positive Integer height,
        Integer rotation,
        TableShape shape
) {
}
