package com.ps.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.ps.restaurant.config.GlobalExceptionHandler;
import com.ps.restaurant.controller.ErrorResponseHandler;
import com.ps.restaurant.dto.ApiError;
import com.ps.restaurant.dto.ErrorResponseException;

/** Checks the structured responses produced directly by each global handler. */
class GlobalExceptionHandlerTests {

    /** Checks that each handler returns its error response instead of throwing another exception. */
    @ParameterizedTest
    @CsvSource({
        "API_EXCEPTION, 403, Error en la api, Api error",
        "VALIDATION_EXCEPTION, 400, Error de validacion, Validation error",
        "MALFORMED_JSON, 400, El cuerpo de la petición es inválido., The request body is invalid.",
        "FORBIDDEN, 403, No tienes permisos para esta acción., You do not have permission to perform this action.",
        "NOT_FOUND, 404, Recurso no encontrado., Resource not found."
    })
    void returnsStructuredErrorsWithLocalizedMessages(String errorCode, int status, String spanish, String english) {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(new ErrorResponseHandler());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        ResponseEntity<ApiError> response = switch (errorCode) {
            case "API_EXCEPTION" -> handler.handleApi(
                    new ErrorResponseException(HttpStatus.FORBIDDEN.value(), errorCode, english), request);
            case "VALIDATION_EXCEPTION" -> {
                MethodArgumentNotValidException validation = mock(MethodArgumentNotValidException.class);
                when(validation.getBindingResult()).thenReturn(new BeanPropertyBindingResult(new Object(), "request"));
                yield handler.handleValidation(validation, request);
            }
            case "MALFORMED_JSON" -> handler.handleUnreadable(mock(HttpMessageNotReadableException.class), request);
            case "FORBIDDEN" -> handler.handleDenied(new AccessDeniedException("Denied"), request);
            case "NOT_FOUND" -> handler.handleNoResource(mock(NoResourceFoundException.class), request);
            default -> throw new IllegalArgumentException(errorCode);
        };

        assertThat(response.getStatusCode().value()).isEqualTo(status);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isFalse();
        assertThat(body.errorCode()).isEqualTo(errorCode);
        assertThat(body.status()).isEqualTo(String.valueOf(status));
        assertThat(body.origin()).isEqualTo(request.getRequestURI());
        assertThat(body.message()).isEqualTo(english);
        assertThat(body.errorMessage())
                .containsExactlyInAnyOrderEntriesOf(Map.of("es", spanish, "en", english));
    }

    @ParameterizedTest
    @CsvSource({"NOT_FOUND, 404", "CONFLICT, 409", "UNAUTHORIZED, 401"})
    void preservesOriginalApiStatusCodeAndMessage(String code, int status) {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(new ErrorResponseHandler());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        ErrorResponseException exception = new ErrorResponseException(status, code, "Original message");

        ResponseEntity<ApiError> response = handler.handleApi(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(status);
        assertThat(response.getBody().errorCode()).isEqualTo(code);
        assertThat(response.getBody().message()).isEqualTo("Original message");
        assertThat(response.getBody().stackTrace()).isEqualTo(exception.getStackTrace());
    }
}
