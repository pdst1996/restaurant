package com.ps.restaurant.dto;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter 
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static class NotFoundException extends ApiException {
        public NotFoundException(String message) {
            super(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
        }
    }

    public static class ConflictException extends ApiException {
        public ConflictException(String message) {
            super(HttpStatus.CONFLICT, "CONFLICT", message);
        }
    }

    public static class UnauthorizedException extends ApiException {
        public UnauthorizedException(String message) {
            super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
        }
    }
}