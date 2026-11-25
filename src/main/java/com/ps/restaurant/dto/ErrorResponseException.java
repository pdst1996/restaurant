package com.ps.restaurant.dto;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

/** Carries an HTTP error status, origin, localized messages, and interpolation metadata for API serialization. */
public class ErrorResponseException extends RuntimeException {

    private String defaultMessageTemplate;

    private Integer status;

    private String origin;

    private String errorCode;
    
    private String message;

    private Object stackTrace;

    private final Map<String, String> errorMessage;
    
    private final Map<String, String> messageVariables;

    /** Stores structured metadata and the supplied map references without setting an exception message or cause. */
    public ErrorResponseException(Integer status, String errorCode, String message) {
        this.defaultMessageTemplate = "";
        this.status = status;
        this.origin = "";
        this.message = message;
        this.errorCode = errorCode;
        this.errorMessage = new HashMap<>();
        this.messageVariables = new HashMap<>();
    }

    /** Stores structured metadata and the supplied map references without setting an exception message or cause. */
    public ErrorResponseException(String defaultMessageTemplate, Integer status, String origin, String errorCode,
            Map<String, String> errorMessage, Map<String, String> messageVariables) {
        this.defaultMessageTemplate = defaultMessageTemplate;
        this.status = status;
        this.origin = origin;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.messageVariables = messageVariables;
    }

    /** Sets the inherited exception message and stores structured metadata and map references. */
    public ErrorResponseException(String message, String defaultMessageTemplate, Integer status, String origin,
            String errorCode, Map<String, String> errorMessage, Map<String, String> messageVariables) {
        super(message);
        this.defaultMessageTemplate = defaultMessageTemplate;
        this.status = status;
        this.origin = origin;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.messageVariables = messageVariables;
    }

    /** Wraps a cause using RuntimeException's cause-derived message and stores the structured metadata. */
    public ErrorResponseException(Throwable cause, String defaultMessageTemplate, Integer status, String origin,
            String errorCode, Map<String, String> errorMessage, Map<String, String> messageVariables) {
        super(cause);
        this.defaultMessageTemplate = defaultMessageTemplate;
        this.status = status;
        this.origin = origin;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.messageVariables = messageVariables;
    }

    /** Preserves an explicit message and underlying cause along with structured error details and map references. */
    public ErrorResponseException(String message, Throwable cause, String defaultMessageTemplate, Integer status,
            String origin, String errorCode, Map<String, String> errorMessage, Map<String, String> messageVariables) {
        super(message, cause);
        this.defaultMessageTemplate = defaultMessageTemplate;
        this.status = status;
        this.origin = origin;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.messageVariables = messageVariables;
    }

    /** Configures suppression and stack-trace behavior while preserving the message, cause, and error metadata. */
    public ErrorResponseException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace, String defaultMessageTemplate, Integer status, String origin, String errorCode,
            Map<String, String> errorMessage, Map<String, String> messageVariables) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.defaultMessageTemplate = defaultMessageTemplate;
        this.status = status;
        this.origin = origin;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.messageVariables = messageVariables;
    }

    /** Represents a missing application resource. */
    public static class NotFoundException extends ErrorResponseException {
        /** Creates a NOT_FOUND exception with HTTP 404 and the supplied message. */
        public NotFoundException(String message) {
            super(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", message);
        }
    }

    /** Represents an operation conflicting with existing application data. */
    public static class ConflictException extends ErrorResponseException {
        /** Creates a CONFLICT exception with HTTP 409 and the supplied message. */
        public ConflictException(String message) {
            super(HttpStatus.CONFLICT.value(), "CONFLICT", message);
        }
    }

    /** Represents missing or invalid authentication. */
    public static class UnauthorizedException extends ErrorResponseException {
        /** Creates an UNAUTHORIZED exception with HTTP 401 and the supplied message. */
        public UnauthorizedException(String message) {
            super(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", message);
        }
    }


    /** Returns the original default message template before placeholder interpolation. */
    public String getDefaultMessageTemplate() {
        return defaultMessageTemplate;
    }

    /** Replaces the stored template without recalculating any already-rendered messages. */
    public void setDefaultMessageTemplate(String defaultMessageTemplate) {
        this.defaultMessageTemplate = defaultMessageTemplate;
    }

    /** Returns the configured HTTP status; the response handler validates it before use. */
    public Integer getStatus() {
        return status;
    }

    /** Replaces the HTTP status metadata without validating its range. */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /** Returns the request URI or application operation associated with the failure. */
    public String getOrigin() {
        return origin;
    }

    /** Updates the failure origin exposed to API clients. */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /** Returns the stable application error code. */
    public String getErrorCode() {
        return errorCode;
    }

    /** Updates the error code without reloading localized messages. */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /** Returns an explicit message override, or the message initialized by the RuntimeException constructor. */
    @Override
    public String getMessage() {
        return message != null ? message : super.getMessage();
    }

    /** Overrides the readable message; null restores the inherited exception-message fallback. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** Returns the diagnostic stack trace included in the API response, if configured. */
     public Object getStackTraceObj() {
        return stackTrace;
    }

    /** Sets diagnostic response metadata without modifying RuntimeException's own stack trace. */
    public void setStackTrace(Object stackTrace) {
        this.stackTrace = stackTrace;
    }

    /** Returns the stored locale-to-message map without creating a defensive copy. */
    public Map<String, String> getErrorMessage() {
        return errorMessage;
    }

    /** Returns the stored placeholder-value map without creating a defensive copy. */
    public Map<String, String> getMessageVariables() {
        return messageVariables;
    }

}
