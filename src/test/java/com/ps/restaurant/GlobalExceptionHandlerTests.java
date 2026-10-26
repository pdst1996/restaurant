package com.ps.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ps.restaurant.config.GlobalExceptionHandler;
import com.ps.restaurant.interfaces.ErrorResponseException;

/** Checks the structured exceptions produced directly by each global handler. */
class GlobalExceptionHandlerTests {

    /** Invokes each handler and checks its code, HTTP status, request origin, and English and Spanish messages. */
    @ParameterizedTest
    @CsvSource({
        "API_EXCEPTION, 403, Error en la api, Api error",
        "VALIDATION_EXCEPTION, 400, Error de validacion, Validation error",
        "MALFORMED_JSON, 400, El cuerpo de la petición es inválido., The request body is invalid.",
        "FORBIDDEN, 403, No tienes permisos para esta acción., You do not have permission to perform this action.",
        "NOT_FOUND, 404, Recurso no encontrado., Resource not found."
    })
    void throwsStructuredErrorsWithLocalizedMessages(String errorCode, int status, String spanish, String english) {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        assertThatThrownBy(() -> {
            switch (errorCode) {
                case "API_EXCEPTION" -> handler.handleApi(null, request);
                case "VALIDATION_EXCEPTION" -> handler.handleValidation(null, request);
                case "MALFORMED_JSON" -> handler.handleUnreadable(request);
                case "FORBIDDEN" -> handler.handleDenied(request);
                case "NOT_FOUND" -> handler.handleNoResource(request);
                default -> throw new IllegalArgumentException(errorCode);
            }
        }).isInstanceOfSatisfying(ErrorResponseException.class, exception -> {
            assertThat(exception.getErrorCode()).isEqualTo(errorCode);
            assertThat(exception.getStatus()).isEqualTo(status);
            assertThat(exception.getOrigin()).isEqualTo(request.getRequestURI());
            assertThat(exception.getMessage()).isEqualTo(english);
            assertThat(exception.getErrorMessage())
                    .containsExactlyInAnyOrderEntriesOf(Map.of("es", spanish, "en", english));
        });
    }
}
