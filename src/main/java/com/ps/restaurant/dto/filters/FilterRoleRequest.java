package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;

public record FilterRoleRequest(
        @Positive Long id,
        String name,
        Boolean active) {
}

