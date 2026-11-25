package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;

public record FilterAllergyRequest(
        @Positive Long id,
        String allergyKey) {
}

