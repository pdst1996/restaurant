package com.ps.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import com.ps.restaurant.interfaces.ErrorResponseException;

class ErrorBuilderTests {

    @ParameterizedTest
    @CsvSource({
        "AUTH_USER_NOT_FOUND, El usuario no existe., The user does not exist.",
        "AUTH_INVALID_PASSWORD, La contraseña es incorrecta., The password is incorrect.",
        "AUTH_EMAIL_ALREADY_EXISTS, El correo ya existe., The email already exists.",
        "AUTH_USER_ALREADY_EXISTS, El usuario ya existe., The username already exists."
    })
    void loadsMessagesFromBothLanguageFiles(String errorCode, String spanish, String english) {
        ErrorResponseException exception = new ErrorBuilder(errorCode, "test")
                .statusCode(HttpStatus.UNAUTHORIZED)
                .createException();

        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getStatus()).isEqualTo(401);
        assertThat(exception.getOrigin()).isEqualTo("test");
        assertThat(exception.getErrorMessage()).containsExactlyInAnyOrderEntriesOf(Map.of("es", spanish, "en", english));
        assertThat(exception.getMessage()).isEqualTo(english);
        assertThat(exception.getMessageVariables()).isEmpty();
    }

    @Test
    void initializesMapsInDefaultConstructor() {
        ErrorBuilder builder = new ErrorBuilder();
        builder.setErrorCode("AUTH_USER_NOT_FOUND");
        builder.setOrigin("test");

        ErrorResponseException exception = builder.addNlsMessage("es", "Usuario {user} no encontrado.")
                .addMessageVariable("user", "Ana")
                .createException();

        assertThat(exception.getStatus()).isEqualTo(500);
        assertThat(exception.getErrorMessage()).containsEntry("es", "Usuario Ana no encontrado.");
        assertThat(exception.getMessageVariables()).containsEntry("user", "Ana");
    }

    @Test
    void acceptsImmutableNlsMapsAndInterpolatesVariables() {
        ErrorBuilder builder = new ErrorBuilder("CUSTOM_ERROR", "Hello {name}", Map.of("es", "Hola {name}"), "test");
        ErrorResponseException exception = builder
                .addNlsMessages(Map.of("en", "Hello {name}"))
                .addMessageVariables(Map.of("name", "Ana"))
                .createException();

        assertThat(exception.getErrorMessage()).containsEntry("es", "Hola Ana").containsEntry("en", "Hello Ana");
        assertThat(exception.getMessage()).isEqualTo("Hello Ana");
    }

    @Test
    void preservesErrorCodeWhenWrappingCause() {
        Exception cause = new IllegalArgumentException("Internal details");
        ErrorResponseException exception = new ErrorBuilder("AUTH_INVALID_PASSWORD", "test")
                .statusCode(401)
                .createException(cause);

        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getErrorCode()).isEqualTo("AUTH_INVALID_PASSWORD");
        assertThat(exception.getErrorMessage()).containsEntry("es", "La contraseña es incorrecta.");
    }

    @Test
    void createdExceptionsDoNotChangeWhenBuilderIsReused() {
        ErrorBuilder builder = new ErrorBuilder("AUTH_USER_NOT_FOUND", "test");
        ErrorResponseException exception = builder.createException();
        builder.addNlsMessage("es", "Changed").addMessageVariable("user", "Ana");

        assertThat(exception.getErrorMessage()).containsEntry("es", "El usuario no existe.");
        assertThat(exception.getMessageVariables()).isEmpty();
    }

    @Test
    void unknownErrorCodeUsesLocalizedFallback() {
        ErrorResponseException exception = new ErrorBuilder("UNKNOWN_ERROR", "test").createException();

        assertThat(exception.getErrorCode()).isEqualTo("UNKNOWN_ERROR");
        assertThat(exception.getErrorMessage()).containsEntry("es", "Ocurrió un error interno.")
                .containsEntry("en", "An internal error occurred.");
    }

    @Test
    void supportsContextCallback() {
        ErrorResponseException exception = new ErrorBuilder("AUTH_USER_NOT_FOUND", "test")
                .createException("login", (context, error) -> error.setOrigin(context.toString()));

        assertThat(exception.getOrigin()).isEqualTo("login");
        assertThat(exception.getErrorCode()).isEqualTo("AUTH_USER_NOT_FOUND");
    }

    @Test
    void throwExceptionThrowsStructuredError() {
        assertThatThrownBy(() -> new ErrorBuilder("AUTH_USER_NOT_FOUND", "test")
                .statusCode(401).throwException())
            .isInstanceOfSatisfying(ErrorResponseException.class,
                    error -> assertThat(error.getErrorCode()).isEqualTo("AUTH_USER_NOT_FOUND"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 200, 600})
    void rejectsInvalidErrorStatuses(int status) {
        assertThatThrownBy(() -> new ErrorBuilder().statusCode(status)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresAnErrorCode() {
        assertThatThrownBy(() -> new ErrorBuilder().createException()).isInstanceOf(IllegalStateException.class);
    }
}
