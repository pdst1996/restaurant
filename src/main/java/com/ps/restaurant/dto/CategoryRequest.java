package com.ps.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Holds the editable fields of a menu category. */
public record CategoryRequest(
        @NotBlank @Size(max = 255) String name) {
}
