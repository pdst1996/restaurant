package com.ps.restaurant.dto;

import java.util.Map;

import com.ps.restaurant.model.ItemAttribute;

/** Exposes an available item attribute without leaking its persistence entity. */
public record ItemAttributeResponse(Long id, String attributeKey, Map<String, String> displayName) {

    public static ItemAttributeResponse from(ItemAttribute attribute) {
        return new ItemAttributeResponse(attribute.getId(), attribute.getAttributeKey(), attribute.getDisplayName());
    }
}
