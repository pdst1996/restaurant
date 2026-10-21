package com.ps.restaurant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ps.restaurant.dto.ApiError;
import com.ps.restaurant.interfaces.ErrorResponseException;

@RestControllerAdvice
public class ErrorResponseHandler {

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> handle(ErrorResponseException exception) {
        Integer configuredStatus = exception.getStatus();
        int status = configuredStatus != null && configuredStatus >= 400 && configuredStatus <= 599
                ? configuredStatus : HttpStatus.INTERNAL_SERVER_ERROR.value();
        ApiError body = ApiError.of( 
                String.valueOf(status),
                exception.getOrigin(),
                exception.getErrorCode(),
                exception.getMessage(),
                exception.getErrorMessage(),
                exception.getMessageVariables());
        return ResponseEntity.status(status).body(body);
    }
}
