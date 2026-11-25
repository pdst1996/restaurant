package com.ps.restaurant.dto;

import java.util.Map;

import com.ps.restaurant.model.ItemCategory;

/** Exposes a category without its bidirectional product relationship. */
public record CategoryResponse(Long id, String categoryKey, Map<String, String> displayName) {

    public static CategoryResponse from(ItemCategory category) {
        return new CategoryResponse(category.getId(), category.getCategoryKey(), category.getDisplayName());
    }
}
