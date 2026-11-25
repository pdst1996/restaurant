package com.ps.restaurant.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LayoutRequest(List<@NotNull @Valid TableLayoutRequest> tables,
        List<@NotNull @Valid ElementLayoutRequest> elements,
        List<@NotNull @Positive Long> deletedElementIds) {
    public LayoutRequest {
        tables = tables == null ? List.of() : tables;
        elements = elements == null ? List.of() : elements;
        deletedElementIds = deletedElementIds == null ? List.of() : deletedElementIds;
    }
}
