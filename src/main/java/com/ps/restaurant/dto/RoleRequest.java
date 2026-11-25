package com.ps.restaurant.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Holds a role's name and activation flag. */
public record RoleRequest(
        @NotBlank @Size(max = 255) String name,
        boolean active,
        @Size(max = 64) Set<@NotBlank @Size(max = 64) String> permissions) {
    public RoleRequest(String name, boolean active) {
        this(name, active, null);
    }
}
