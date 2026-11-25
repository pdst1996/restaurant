package com.ps.restaurant.dto;

import java.util.Set;

import com.ps.restaurant.model.Role;

/** Exposes a role's identity, name, and activation flag. */
public record RoleResponse(Long id, String name, boolean active, Set<String> permissions) {
    public RoleResponse(Long id, String name, boolean active) {
        this(id, name, active, Set.of());
    }

    public static RoleResponse from(Role role){
        return new RoleResponse(role.getId(), role.getName(), role.isActive(), Set.copyOf(role.getPermissions()));
    }
}
