package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Holds item fields and references existing categories, attributes, and sizes by identifier. */
public record ItemRequest (
        @NotBlank @Size(max = 255) String sku,
        @NotEmpty Map<@NotBlank String, @NotBlank String> displayName,
        Map<@NotBlank String, Object> barcodes,
        @Size(max = 255) String description,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull @Positive Long categoryId,
        boolean available,
        @Size(max = 2048) String img,
        List<@NotNull @Positive Long> attributeIds,
        List<@NotNull @Positive Long> sizeIds) {
}
