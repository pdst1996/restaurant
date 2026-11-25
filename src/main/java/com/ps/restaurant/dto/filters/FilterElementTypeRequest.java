package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;

public record FilterElementTypeRequest(@Positive Long id, String type, Boolean round) {
}
