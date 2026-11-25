package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;

public record FilterUserRequest(
        @Positive Long id,
        String email,
        String user,
        String fullName,
        @Positive Long roleId,
        Boolean active) {
}

