package com.ps.restaurant.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Wraps successful API data and an optional message; record accessors expose its immutable components. */
@JsonInclude (JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data, Instant timestamp) {

    /** Creates a successful response containing data and the current timestamp, without a message. */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data, Instant.now());
    }

    /** Creates a successful response containing the supplied message, data, and current timestamp. */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }
}
