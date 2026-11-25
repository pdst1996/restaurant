package com.ps.restaurant.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** Holds an item size key and its localized names. */
public record ItemSizeRequest(
        @NotBlank @Size(max = 255) String sizeKey,
        @NotEmpty Map<@NotBlank String, @NotBlank String> displayName) {
}
