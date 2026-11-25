package com.ps.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Holds a username-or-email identifier and password; Java generates the constructor and component accessors. */
public record LoginRequest(@NotBlank @Size(max = 255) String user, @NotBlank @Size(max = 72) String password) {

}
