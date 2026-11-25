package com.ps.restaurant.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.ps.restaurant.model.Item;

/** Exposes item details and catalog DTOs without the inverse order-items relationship. */
public record ItemResponse(
        Long id,
        String sku,
        Map<String, String> displayName,
        Map<String, Object> barcodes,
        String description,
        BigDecimal price,
        Long categoryId,
        boolean available,
        String img,
        List<ItemAttributeResponse> availableAttributes,
        List<ItemSizeResponse> sizes,
        boolean active) {
    public ItemResponse(Long id, String sku, Map<String, String> displayName, Map<String, Object> barcodes,
            String description, BigDecimal price, Long categoryId, boolean available, String img,
            List<ItemAttributeResponse> availableAttributes, List<ItemSizeResponse> sizes) {
        this(id, sku, displayName, barcodes, description, price, categoryId, available, img,
                availableAttributes, sizes, true);
    }

    public static ItemResponse from(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getSku(),
                item.getDisplayName(),
                item.getBarcodes(),
                item.getDescription(),
                item.getPrice(),
                item.getCategory().getId(),
                item.isAvailable(),
                item.getImg(),
                item.getAvailableAttributes() == null ? List.of()
                        : item.getAvailableAttributes().stream().map(ItemAttributeResponse::from).toList(),
                item.getSizes() == null ? List.of()
                        : item.getSizes().stream().map(ItemSizeResponse::from).toList(), item.isActive());
    }
}
