package com.ps.restaurant.dto;

import com.ps.restaurant.model.Role;

/** Holds registration fields and the requested role; Java generates its constructor and component accessors. */
public record UserRequest(String email, String password, String user, String fullName, Role role) {
    
}

