package com.ps.restaurant.dto;

import java.util.Map;

import com.ps.restaurant.model.ItemSize;

/** Exposes an item size without leaking its persistence entity. */
public record ItemSizeResponse(Long id, String sizeKey, Map<String, String> displayName) {

    public static ItemSizeResponse from(ItemSize size) {
        return new ItemSizeResponse(size.getId(), size.getSizeKey(), size.getDisplayName());
    }
}
