package com.suguna.weighment_api_service.exception;

import lombok.Getter;

import java.util.Map;

/**
 * HTTP 422 — business rule rejection (e.g. invalid OTP). Uses a domain {@code errorCode} string.
 */
@Getter
public class BusinessValidationException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> details;

    public BusinessValidationException(String errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public BusinessValidationException(String errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details == null ? Map.of() : details;
    }
}
