package com.ps.restaurant.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.ps.restaurant.dto.ApiError;
import com.ps.restaurant.dto.ApiException;
import com.ps.restaurant.service.ErrorBuilder;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Replaces selected MVC and API exceptions with localized structured exceptions.
 * Exceptions thrown here are not automatically dispatched to another exception handler.
 */
@RestControllerAdvice 
@Slf4j 
public class GlobalExceptionHandler {

    /** Throws an API_EXCEPTION error with HTTP 403 and the request URI, regardless of the original status. */
    @ExceptionHandler (ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex, HttpServletRequest req) {
        throw new ErrorBuilder("API_EXCEPTION", req.getRequestURI())
                    .statusCode(HttpStatus.FORBIDDEN)
                    .createException();
    }

    /** Replaces argument validation failures with a localized HTTP 400 validation exception. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        throw new ErrorBuilder("VALIDATION_EXCEPTION", req.getRequestURI())
                    .statusCode(HttpStatus.BAD_REQUEST)
                    .createException();
    }

    /** Replaces unreadable request bodies with a localized HTTP 400 malformed-body exception. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpServletRequest req) {
        throw new ErrorBuilder("MALFORMED_JSON", req.getRequestURI())
                    .statusCode(HttpStatus.BAD_REQUEST)
                    .createException();
    }

    /** Replaces Spring Security access-denied failures with a localized HTTP 403 exception. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleDenied(HttpServletRequest req) {
        throw new ErrorBuilder("FORBIDDEN", req.getRequestURI())
                    .statusCode(HttpStatus.FORBIDDEN)
                    .createException();
    }

    /** Replaces missing MVC resources with a localized HTTP 404 exception. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(HttpServletRequest req) {
        throw new ErrorBuilder("NOT_FOUND", req.getRequestURI())
                    .statusCode(HttpStatus.NOT_FOUND)
                    .createException();
    }
}
