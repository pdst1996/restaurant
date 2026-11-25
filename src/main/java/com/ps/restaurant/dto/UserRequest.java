package com.ps.restaurant.dto;

import jakarta.validation.Valid;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Holds registration fields and the requested role; Java generates its constructor and component accessors. */
public record UserRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 72) String password,
        @NotBlank @Size(max = 255) String user,
        @NotBlank @Size(max = 255) String fullName,
        @NotNull @Valid RoleReferenceRequest role) {
    
}

