package com.ps.restaurant.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

import org.springframework.http.HttpStatus;

import com.ps.restaurant.interfaces.ErrorResponseException;

public class ErrorBuilder {

    private static final List<String> SUPPORTED_LANGUAGES = List.of("es", "en");

    private Integer status = HttpStatus.INTERNAL_SERVER_ERROR.value();

    private String origin;

    private String errorCode;

    private String message;

    private final Map<String, String> errorMessage;
    
    private final Map<String, String> messageVariables;

    public ErrorBuilder() {
        this.messageVariables = new HashMap<>();
        this.errorMessage = new HashMap<>();
        this.message = "";
    }

    public ErrorBuilder(String errorCode, String origin) {
        this(errorCode, null, origin);
    }

    public ErrorBuilder(String errorCode, String message, String origin) {
        this();
        this.errorCode = errorCode;
        this.message = message;
        this.origin = origin;
    }

    public ErrorBuilder(String errorCode, String message, Map<String, String> errorMessage, String origin) {
        this(errorCode, message, origin);
        this.message = errorMessage == null || errorMessage.get("default") == null
                ? message
                : errorMessage.get("default");
        if (errorMessage != null) {
            this.errorMessage.putAll(errorMessage);
        }
    }

    /**
     * Add a message variable which will be used as substitution for occurrences in
     * the message template.
     *
     * @param key   The key which exists in the form of {key} in the message
     *              template
     * @param value The value to use when substituting the key occurrence
     * @return This instance of {@code ErrorBuilder}
     */
    public ErrorBuilder addMessageVariable(String key, String value) {
        messageVariables.put(key, value);
        return this;
    }

    public ErrorBuilder addMessageVariables(Map<String, String> messageVariables) {
        this.messageVariables.putAll(messageVariables);
        return this;
    }

    public ErrorBuilder addNlsMessages(Map<String, String> errorMessage) {
        this.errorMessage.putAll(errorMessage);
        return this;
    }

    public ErrorBuilder addNlsMessage(String locale, String message) {
        this.errorMessage.put(locale, message);
        return this;
    }

    public ErrorBuilder statusCode(int status) {
        if (status < 400 || status > 599) {
            throw new IllegalArgumentException("An error status must be between 400 and 599");
        }
        this.status = status;
        return this;
    }

    public ErrorBuilder statusCode(HttpStatus status) {
        return statusCode(Objects.requireNonNull(status, "status").value());
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

     public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Creates a new instance of {@code ErrorResponseException} and throws it.
     *
     * @throws ErrorResponseException clientErrorResponseException
     */
    public void throwException() throws ErrorResponseException {
        throw createException();
    }

    /**
     * Creates a {@code ErrorResponseException}.
     *
     * @return {@link ErrorResponseException}
     */
    public ErrorResponseException createException() {
        return createException((Exception) null);
    }

    /**
     * Creates a {@code ErrorResponseException}.
     *
     * @return {@link ErrorResponseException}
     */
    public ErrorResponseException createException(Object context,
            BiConsumer<Object, ErrorResponseException> consumer) {
        ErrorResponseException errorException = createException();
        if (consumer != null) {
            consumer.accept(context, errorException);
        }
        return errorException;
    }

    /**
     * Creates a {@code ErrorResponseException} with a given cause exception.
     *
     * @return {@link ErrorResponseException}
     */
    public ErrorResponseException createException(Exception cause) {
        if (errorCode == null || errorCode.isBlank()) {
            throw new IllegalStateException("An errorCode is required");
        }
        Map<String, String> messages = loadMessages();
        String template = message != null ? message : messages.get("en");
        messages.replaceAll((language, message) -> interpolate(message));
        return new ErrorResponseException(interpolate(template), cause, template, status, origin, errorCode,
                Map.copyOf(messages), Map.copyOf(messageVariables));
    }

    private Map<String, String> loadMessages() {
        Map<String, String> messages = new HashMap<>();
        for (String language : SUPPORTED_LANGUAGES) {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.errors", Locale.forLanguageTag(language),
                    ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
            String defaultMessageString = bundle.containsKey(errorCode)
                    ? bundle.getString(errorCode)
                    : message != null ? message : bundle.getString("INTERNAL_ERROR");
            messages.put(language, defaultMessageString);
        }
        messages.putAll(errorMessage);
        return messages;
    }

    private String interpolate(String template) {
        String message = template;
        for (Map.Entry<String, String> variable : messageVariables.entrySet()) {
            message = message.replace("{" + variable.getKey() + "}", variable.getValue());
        }
        return message;
    }
}
