package com.ps.restaurant.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** Holds an allergy key and its localized names. */
public record AllergyRequest(
        @NotBlank @Size(max = 255) String allergyKey,
        @NotEmpty Map<@NotBlank String, @NotBlank String> displayName) {
}
