package com.suguna.weighment_api_service.constants;

import org.springframework.http.HttpStatus;

/**
 * Standard API errors with HTTP status and retry hint for the mobile client.
 */
public enum ErrorCode {

    BAD_REQUEST(ErrorCodes.BAD_REQUEST, "Invalid request", HttpStatus.BAD_REQUEST, false),
    VALIDATION_FAILED(ErrorCodes.VALIDATION_FAILED, "Validation failed", HttpStatus.BAD_REQUEST, false),
    INVALID_JSON(ErrorCodes.INVALID_JSON, "Malformed JSON request body", HttpStatus.BAD_REQUEST, false),

    UNAUTHORIZED(ErrorCodes.UNAUTHORIZED, "Authentication failed", HttpStatus.UNAUTHORIZED, false),
    FORBIDDEN(ErrorCodes.FORBIDDEN, "Access denied", HttpStatus.FORBIDDEN, false),
    ACTIVATION_REQUIRED(
            ErrorCodes.ACTIVATION_REQUIRED,
            "Device is not configured in ERP",
            HttpStatus.FORBIDDEN,
            false),

    NOT_FOUND(ErrorCodes.NOT_FOUND, "Reference not found", HttpStatus.NOT_FOUND, false),
    CONFLICT(ErrorCodes.CONFLICT, "Resource conflict", HttpStatus.CONFLICT, false),
    DUPLICATE_IDEMPOTENCY_KEY(
            ErrorCodes.DUPLICATE_IDEMPOTENCY_KEY,
            "Duplicate idempotency key",
            HttpStatus.CONFLICT,
            false),
    SCHEDULE_CLOSED(ErrorCodes.SCHEDULE_CLOSED, "Schedule is already closed", HttpStatus.CONFLICT, false),

    BUSINESS_VALIDATION_FAILED(
            ErrorCodes.BUSINESS_VALIDATION_FAILED,
            "Business validation failed",
            HttpStatus.UNPROCESSABLE_ENTITY,
            false),

    INTERNAL_ERROR(ErrorCodes.INTERNAL_ERROR, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, true),
    SERVICE_UNAVAILABLE(
            ErrorCodes.SERVICE_UNAVAILABLE,
            "Service temporarily unavailable",
            HttpStatus.SERVICE_UNAVAILABLE,
            true),
    ERP_UNAVAILABLE(ErrorCodes.ERP_UNAVAILABLE, "ERP is unavailable", HttpStatus.SERVICE_UNAVAILABLE, true);

    private final String errorCode;
    private final String defaultMessage;
    private final HttpStatus httpStatus;
    private final boolean retryable;

    ErrorCode(String errorCode, String defaultMessage, HttpStatus httpStatus, boolean retryable) {
        this.errorCode = errorCode;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
