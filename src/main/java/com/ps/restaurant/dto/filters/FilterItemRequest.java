package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;

public record FilterItemRequest(
    @Positive Long id,
    String sku,
    String barcode,
    @Positive Long categoryId,
    Boolean available
) {}
