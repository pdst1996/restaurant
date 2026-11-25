package com.ps.restaurant.dto;

import com.ps.restaurant.model.User;

/** Exposes account identity, role, and activation details without including a password. */
public record UserResponse(Long id, String email, String user, String fullName, RoleResponse role, boolean active) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getUser(),
            user.getFullName(),
            RoleResponse.from(user.getRole()),
            user.isActive()
        );
    }

}
