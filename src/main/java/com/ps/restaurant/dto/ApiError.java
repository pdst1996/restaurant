package com.ps.restaurant.dto;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude (JsonInclude.Include.NON_NULL)
public record ApiError(boolean success, String status, String origin, String errorCode, String message, Map<String, String> errorMessage, Map<String, String> messageVariables, Date timestamp) {
    public static ApiError of(String status, String origin, String errorCode, String message,
                              Map<String, String> errorMessage, Map<String, String> messageVariables) {
        return new ApiError(false, status, origin, errorCode, message, errorMessage, messageVariables, Date.from(Instant.now()));
    }
}
