package com.ps.restaurant.dto;

import com.ps.restaurant.model.Table;
import com.ps.restaurant.model.TableShape;
import com.ps.restaurant.model.TableStatus;

/** Exposes a restaurant table without its bidirectional order relationship. */
public record TableResponse(Long id, String tableName, Integer capacity, TableStatus status,
                       FloorResponse floor, Integer x, Integer y, Integer width, Integer height,
                       Integer rotation, TableShape shape) {

    public static TableResponse from(Table table) {
        return new TableResponse(
                table.getId(),
                table.getTableName(),
                table.getCapacity(),
                table.getTableStatus(),
                FloorResponse.from(table.getFloor()),
                table.getX(),
                table.getY(),
                table.getWidth(),
                table.getHeight(),
                table.getRotation(),
                table.getShape()
            );
    }
}
