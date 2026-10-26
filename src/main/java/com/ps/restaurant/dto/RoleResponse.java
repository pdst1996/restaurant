package com.ps.restaurant.dto;

/** Exposes a role's identity, name, and activation flag. */
public record RoleResponse(Long id, String name, boolean active) {
}
