package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;

public record FilterCategoryRequest(
        @Positive Long id,
        String categoryKey) {
}

