package com.ps.restaurant.interfaces;

import java.util.Map;

public class ErrorResponseException extends RuntimeException {

    private String defaultMessageTemplate;

    private Integer status;

    private String origin;

    private String errorCode;
    
    private String message;

    private final Map<String, String> errorMessage;
    
    private final Map<String, String> messageVariables;

    public String getDefaultMessageTemplate() {
        return defaultMessageTemplate;
    }

    public void setDefaultMessageTemplate(String defaultMessageTemplate) {
        this.defaultMessageTemplate = defaultMessageTemplate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getErrorMessage() {
        return errorMessage;
    }

    public Map<String, String> getMessageVariables() {
        return messageVariables;
    }

    public ErrorResponseException(String defaultMessageTemplate, Integer status, String origin, String errorCode,
            Map<String, String> errorMessage, Map<String, String> messageVariables) {
        this.defaultMessageTemplate = defaultMessageTemplate;
        this.status = status;
        this.origin = origin;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.messageVariables = messageVariables;
    }

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

}
