package com.ps.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record FloorRequest(
    @NotBlank @Size(max = 255) String name, 
    @Positive Integer width,
    @Positive Integer height,
    @PositiveOrZero Integer sort_order
) {

}
