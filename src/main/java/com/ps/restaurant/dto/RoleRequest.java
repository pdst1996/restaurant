package com.ps.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Holds a role's name and activation flag. */
public record RoleRequest(
        @NotBlank @Size(max = 255) String name,
        boolean active) {
}
