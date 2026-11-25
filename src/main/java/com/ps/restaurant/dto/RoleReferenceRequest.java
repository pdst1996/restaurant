package com.ps.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Accepts only an existing role identifier, never a role entity from the client. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RoleReferenceRequest(@NotNull @Positive Long id) {
}
