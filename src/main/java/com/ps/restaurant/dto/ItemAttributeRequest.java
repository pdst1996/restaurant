package com.ps.restaurant.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** Holds an item attribute key and its localized names. */
public record ItemAttributeRequest(
        @NotBlank @Size(max = 255) String attributeKey,
        @NotEmpty Map<@NotBlank String, @NotBlank String> displayName) {
}
