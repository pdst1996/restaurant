package com.ps.restaurant.dto;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/** Carries an HTTP status and application code; Lombok generates read-only accessors for both fields. */
@Getter 
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    /** Stores the status and application code while passing the readable message to RuntimeException. */
    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    /** Represents a missing application resource. */
    public static class NotFoundException extends ApiException {
        /** Creates a NOT_FOUND exception with HTTP 404 and the supplied message. */
        public NotFoundException(String message) {
            super(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
        }
    }

    /** Represents an operation conflicting with existing application data. */
    public static class ConflictException extends ApiException {
        /** Creates a CONFLICT exception with HTTP 409 and the supplied message. */
        public ConflictException(String message) {
            super(HttpStatus.CONFLICT, "CONFLICT", message);
        }
    }

    /** Represents missing or invalid authentication. */
    public static class UnauthorizedException extends ApiException {
        /** Creates an UNAUTHORIZED exception with HTTP 401 and the supplied message. */
        public UnauthorizedException(String message) {
            super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
        }
    }
}
