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

/** Builds structured error exceptions from localized resource bundles and optional message overrides. */
public class ErrorBuilder {

    private static final List<String> SUPPORTED_LANGUAGES = List.of("es", "en");

    private Integer status = HttpStatus.INTERNAL_SERVER_ERROR.value();

    private String origin;

    private String errorCode;

    private String message;

    private final Map<String, String> errorMessage;
    
    private final Map<String, String> messageVariables;

    /** Initializes mutable message maps, an empty default message, and the default HTTP 500 status. */
    public ErrorBuilder() {
        this.messageVariables = new HashMap<>();
        this.errorMessage = new HashMap<>();
        this.message = "";
    }

    /** Selects an error code and origin, allowing the English bundle to supply the default message. */
    public ErrorBuilder(String errorCode, String origin) {
        this(errorCode, null, origin);
    }

    /** Selects an error code, an optional default message template, and the failure origin. */
    public ErrorBuilder(String errorCode, String message, String origin) {
        this();
        this.errorCode = errorCode;
        this.message = message;
        this.origin = origin;
    }

    /** Copies localized overrides and prefers their non-null "default" entry over the supplied message. */
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

    /** Copies placeholder values into this builder, replacing existing values for matching keys. */
    public ErrorBuilder addMessageVariables(Map<String, String> messageVariables) {
        this.messageVariables.putAll(messageVariables);
        return this;
    }

    /** Copies locale-specific message overrides, replacing existing entries for matching locales. */
    public ErrorBuilder addNlsMessages(Map<String, String> errorMessage) {
        this.errorMessage.putAll(errorMessage);
        return this;
    }

    /** Adds or replaces a message template for one locale and returns this builder. */
    public ErrorBuilder addNlsMessage(String locale, String message) {
        this.errorMessage.put(locale, message);
        return this;
    }

    /** Sets an HTTP error status, rejecting any value outside the inclusive 400-599 range. */
    public ErrorBuilder statusCode(int status) {
        if (status < 400 || status > 599) {
            throw new IllegalArgumentException("An error status must be between 400 and 599");
        }
        this.status = status;
        return this;
    }

    /** Requires a non-null Spring HTTP status and delegates to the numeric error-status validation. */
    public ErrorBuilder statusCode(HttpStatus status) {
        return statusCode(Objects.requireNonNull(status, "status").value());
    }

    /** Returns the code used to look up localized message templates. */
    public String getErrorCode() {
        return errorCode;
    }

    /** Replaces the message lookup code; a nonblank value is required when creating an exception. */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /** Returns the configured failure origin, such as a request URI or controller operation. */
    public String getOrigin() {
        return origin;
    }

    /** Updates the origin that will be copied into subsequently created exceptions. */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

     /** Returns the explicit default template, which may be null to enable the English bundle fallback. */
     public String getMessage() {
        return message;
    }

    /** Replaces the default template; a null value selects the English localized message at creation time. */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Creates a localized structured exception using the current builder state and throws it.
     *
     * @throws ErrorResponseException always, after successful exception construction
     */
    public void throwException() throws ErrorResponseException {
        throw createException();
    }

    /**
     * Creates a localized exception without an underlying cause.
     *
     * @return {@link ErrorResponseException}
     */
    public ErrorResponseException createException() {
        return createException((Exception) null);
    }

    /**
     * Creates an exception and, if provided, invokes the callback with the context
     * and new exception before returning it.
     *
     * @param context caller-defined data passed unchanged to the callback
     * @param consumer optional callback that can customize the created exception
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
     * Requires a nonblank code, loads and interpolates messages, and creates an exception
     * with immutable snapshots of the localized messages and placeholder values.
     *
     * @param cause optional underlying exception to preserve
     * @return {@link ErrorResponseException}
     * @throws IllegalStateException if the error code is missing or blank
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

    /**
     * Loads Spanish and English templates for the code, falling back to the explicit
     * message or INTERNAL_ERROR, then applies caller-provided locale overrides.
     */
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

    /** Replaces each configured {key} placeholder with its value, leaving unknown placeholders unchanged. */
    private String interpolate(String template) {
        String message = template;
        for (Map.Entry<String, String> variable : messageVariables.entrySet()) {
            message = message.replace("{" + variable.getKey() + "}", variable.getValue());
        }
        return message;
    }
}
