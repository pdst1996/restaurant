package com.ps.restaurant.dto;

import java.util.List;

public record LayoutResponse(List<TableResponse> tables, List<ElementPlanResponse> elements) {
}
