package com.ps.restaurant.dto;

import java.util.Map;

import com.ps.restaurant.model.ElementType;

public record ElementTypeResponse(Long id, String type, Map<String, String> label, Integer width, Integer height,
        boolean round, String fill, String stroke) {
    public static ElementTypeResponse from(ElementType e) {
        return new ElementTypeResponse(e.getId(), e.getType(), e.getLabel(), e.getWidth(), e.getHeight(), e.isRound(),
                e.getFill(), e.getStroke());
    }
}
