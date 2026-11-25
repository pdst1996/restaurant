package com.ps.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record UserUpdateRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @Size(min = 1, max = 72) String password,
        @NotBlank @Size(max = 255) String user,
        @NotBlank @Size(max = 255) String fullName,
        @NotNull @Valid RoleReferenceRequest role,
        @NotNull Boolean active) {}
