package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;

public record FilterItemAttributeRequest(
        @Positive Long id,
        String attributeKey) {
}

