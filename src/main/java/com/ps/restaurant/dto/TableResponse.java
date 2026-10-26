package com.ps.restaurant.dto;

/** Exposes a restaurant table without its bidirectional order relationship. */
public record TableResponse(Long id, Integer tableNumber, Integer capacity) {
}
