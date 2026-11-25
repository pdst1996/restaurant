package com.ps.restaurant.dto.filters;

import jakarta.validation.constraints.Positive;
import com.ps.restaurant.model.TableShape;
import com.ps.restaurant.model.TableStatus;

public record FilterTableRequest(@Positive Long id, String tableName, @Positive Integer capacity,
        @Positive Long floorId, TableShape shape, TableStatus status) {

}
