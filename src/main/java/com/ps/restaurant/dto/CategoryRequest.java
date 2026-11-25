package com.ps.restaurant.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** Holds the editable fields of a menu category. */
public record CategoryRequest(
        @NotBlank @Size(max = 255) String categoryKey,
        @NotEmpty Map<@NotBlank String, @NotBlank String> displayName) {
}
