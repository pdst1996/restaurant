package com.ps.restaurant.config;

import org.springframework.http.HttpStatus;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.ps.restaurant.controller.ErrorResponseHandler;
import com.ps.restaurant.dto.ApiError;
import com.ps.restaurant.dto.ErrorResponseException;
import com.ps.restaurant.service.ErrorBuilder;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Returns localized structured responses for selected MVC and API exceptions.
 */
@RestControllerAdvice 
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorResponseHandler errorResponseHandler;

    /** Preserves the API exception's status, code, and message in a structured response. */
    @ExceptionHandler (ErrorResponseException.class)
    public ResponseEntity<ApiError> handleApi(ErrorResponseException ex, HttpServletRequest req) {
        if (ex.getErrorMessage() != null && !ex.getErrorMessage().isEmpty()) {
            return errorResponseHandler.handle(ex);
        }
        return errorResponseHandler.handle(new ErrorBuilder(ex.getErrorCode(), ex.getMessage(), req.getRequestURI())
                    .statusCode(ex.getStatus())
                    .createException(ex));
    }

    /** Returns a localized HTTP 400 response for argument validation failures. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        return errorResponseHandler.handle(new ErrorBuilder("VALIDATION_EXCEPTION", req.getRequestURI(), ex.getStackTrace())
                    .statusCode(HttpStatus.BAD_REQUEST)
                    .createException(ex));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return errorResponseHandler.handle(new ErrorBuilder("VALIDATION_EXCEPTION", req.getRequestURI(), ex.getStackTrace())
                .statusCode(HttpStatus.BAD_REQUEST).createException(ex));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return errorResponseHandler.handle(new ErrorBuilder("CONFLICT", req.getRequestURI(), ex.getStackTrace())
                .statusCode(HttpStatus.CONFLICT).createException(ex));
    }

    /** Returns a localized HTTP 400 response for unreadable request bodies. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return errorResponseHandler.handle(new ErrorBuilder("MALFORMED_JSON", req.getRequestURI(), ex.getStackTrace())
                    .statusCode(HttpStatus.BAD_REQUEST)
                    .createException(ex));
    }

    /** Returns a localized HTTP 403 response for access-denied failures. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
        return errorResponseHandler.handle(new ErrorBuilder("FORBIDDEN", req.getRequestURI(), ex.getStackTrace())
                    .statusCode(HttpStatus.FORBIDDEN)
                    .createException(ex));
    }

    /** Returns a localized HTTP 404 response for missing MVC resources. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        return errorResponseHandler.handle(new ErrorBuilder("NOT_FOUND", req.getRequestURI(), ex.getStackTrace())
                    .statusCode(HttpStatus.NOT_FOUND)
                    .createException(ex));
    }
}
