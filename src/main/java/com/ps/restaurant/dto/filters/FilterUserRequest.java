package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public record FilterUserRequest(
        @Positive Long id,
        String email,
        String user,
        String fullName,
        @Positive Long roleId,
        Boolean active,
        @Size(max = 100) String search,
        @PositiveOrZero Integer page,
        @Positive @Max(100) Integer size) {
    public FilterUserRequest(Long id, String email, String user, String fullName, Long roleId, Boolean active) {
        this(id, email, user, fullName, roleId, active, null, null, null);
    }
}

