package com.suguna.weighment_api_service.integration.oracle;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

/** Resolves expected shop OTP from DTL columns or legacy {@code MESSAGE} token. */
public final class RetailRetailerOtpSupport {

    private RetailRetailerOtpSupport() {}

    public static RetailRetailerOtpResolution resolve(
            boolean useDtlOtpColumn,
            String dtlOtp,
            Instant otpExpiresAt,
            String message,
            Instant now) {
        if (useDtlOtpColumn && dtlOtp != null && !dtlOtp.isBlank()) {
            Instant checkTime = now == null ? Instant.now() : now;
            if (otpExpiresAt != null && checkTime.isAfter(otpExpiresAt)) {
                return RetailRetailerOtpResolution.expiredOtp();
            }
            return RetailRetailerOtpResolution.active(dtlOtp.trim());
        }
        return extractOtpFromMessage(message)
                .map(RetailRetailerOtpResolution::active)
                .orElseGet(RetailRetailerOtpResolution::notConfigured);
    }

    static Optional<String> extractOtpFromMessage(String message) {
        if (message == null) {
            return Optional.empty();
        }
        String upper = message.toUpperCase(Locale.ROOT);
        int idx = upper.indexOf("RETAIL_OTP:");
        if (idx < 0) {
            return Optional.empty();
        }
        String tail = message.substring(idx + "RETAIL_OTP:".length());
        int end = tail.indexOf('|');
        String otp = (end > 0 ? tail.substring(0, end) : tail).trim();
        return otp.isEmpty() ? Optional.empty() : Optional.of(otp);
    }
}
