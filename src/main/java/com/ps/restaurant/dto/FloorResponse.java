package com.ps.restaurant.dto;

import com.ps.restaurant.model.Floor;

public record FloorResponse(Long id, String name, Integer width, Integer height, Integer sort_order) {
    public static FloorResponse from(Floor f) {
        return new FloorResponse(f.getId(), f.getName(), f.getWidth(), f.getHeight(), f.getSort_order());
    } 
}
