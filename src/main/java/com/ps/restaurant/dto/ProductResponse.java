package com.ps.restaurant.dto;

import java.math.BigDecimal;

/** Exposes a product with its category identifier instead of persistence entities. */
public record ProductResponse(
        Long id, String sku, String name, BigDecimal price, Long categoryId, boolean available) {
}
