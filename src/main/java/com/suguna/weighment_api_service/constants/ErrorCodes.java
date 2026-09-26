package com.suguna.weighment_api_service.constants;

/**
 * Stable {@code errorCode} values returned to the Flutter client.
 * Extend this catalog as ERP integration rules are finalized.
 */
public final class ErrorCodes {

    private ErrorCodes() {
    }

    // --- Request / transport (typically HTTP 400) ---
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String INVALID_JSON = "INVALID_JSON";

    // --- Auth / access (401 / 403) ---
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String ACTIVATION_REQUIRED = "ACTIVATION_REQUIRED";

    // --- Reference / state (404 / 409) ---
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String DUPLICATE_IDEMPOTENCY_KEY = "DUPLICATE_IDEMPOTENCY_KEY";
    public static final String SCHEDULE_CLOSED = "SCHEDULE_CLOSED";

    // --- Business validation (HTTP 422) ---
    public static final String INVALID_RETAILER_OTP = "INVALID_RETAILER_OTP";
    public static final String INVALID_FARMER_OTP = "INVALID_FARMER_OTP";
    public static final String INVALID_TRADER_OTP = "INVALID_TRADER_OTP";
    public static final String BUSINESS_VALIDATION_FAILED = "BUSINESS_VALIDATION_FAILED";

    // --- Infrastructure / ERP (500 / 503, often retryable) ---
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    public static final String ERP_UNAVAILABLE = "ERP_UNAVAILABLE";
}
