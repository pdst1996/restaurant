package com.ps.restaurant.dto;

import com.ps.restaurant.model.Element;

public record ElementPlanResponse(Long id, Long floorId, String type, Integer x, Integer y,
        Integer width, Integer height, Integer rotation, String label, ElementTypeResponse elementType) {
    public static ElementPlanResponse from(Element e) {
        return new ElementPlanResponse(e.getId(), e.getFloor().getId(), e.getElementType().getType(), e.getX(), e.getY(), e.getWidth(),
                e.getHeight(), e.getRotation(), e.getLabel(), ElementTypeResponse.from(e.getElementType()));
    }
}
