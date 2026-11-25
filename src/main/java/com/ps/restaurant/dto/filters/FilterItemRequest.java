package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Max;

public record FilterItemRequest(
    @Positive Long id,
    String sku,
    String barcode,
    @Positive Long categoryId,
    Boolean available,
    Boolean includeInactive,
    @PositiveOrZero Integer page,
    @Positive @Max(100) Integer size
) {
    public FilterItemRequest(Long id, String sku, String barcode, Long categoryId, Boolean available) {
        this(id, sku, barcode, categoryId, available, null, null, null);
    }
}
