package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;

public record FilterItemSizeRequest(
        @Positive Long id,
        String sizeKey) {
}

