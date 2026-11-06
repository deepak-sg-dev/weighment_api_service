package com.suguna.weighment_api_service.integration.oracle;

/** Expected shop OTP for a retail order line (DTL column or legacy {@code MESSAGE} token). */
public record RetailRetailerOtpResolution(boolean configured, boolean expired, String expectedCode) {

    public static RetailRetailerOtpResolution notConfigured() {
        return new RetailRetailerOtpResolution(false, false, null);
    }

    public static RetailRetailerOtpResolution active(String expectedCode) {
        return new RetailRetailerOtpResolution(true, false, expectedCode);
    }

    public static RetailRetailerOtpResolution expiredOtp() {
        return new RetailRetailerOtpResolution(true, true, null);
    }
}
