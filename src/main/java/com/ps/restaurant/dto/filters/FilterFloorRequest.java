package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;

public record FilterFloorRequest(@Positive Long id, String name) {

}
