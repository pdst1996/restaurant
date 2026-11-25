package com.ps.restaurant.dto;

import com.ps.restaurant.model.Role;

/** Exposes a role's identity, name, and activation flag. */
public record RoleResponse(Long id, String name, boolean active) {

    public static RoleResponse from(Role role){
        return new RoleResponse(role.getId(), role.getName(), role.isActive());
    }
}
