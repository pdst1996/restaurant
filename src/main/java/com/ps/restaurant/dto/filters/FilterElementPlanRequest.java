package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;

public record FilterElementPlanRequest(@Positive Long id, String type, String label, @Positive Long floorId) {

}
