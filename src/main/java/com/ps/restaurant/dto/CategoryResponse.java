package com.ps.restaurant.dto;

/** Exposes a category without its bidirectional product relationship. */
public record CategoryResponse(Long id, String name) {
}
