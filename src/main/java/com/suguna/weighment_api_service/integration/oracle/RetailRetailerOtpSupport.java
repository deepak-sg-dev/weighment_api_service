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
        return resolve(useDtlOtpColumn, dtlOtp, otpExpiresAt, message, now, null);
    }

    public static RetailRetailerOtpResolution resolve(
            boolean useDtlOtpColumn,
            String dtlOtp,
            Instant otpExpiresAt,
            String message,
            Instant now,
            String stubWhenUnset) {
        if (useDtlOtpColumn && dtlOtp != null && !dtlOtp.isBlank()) {
            Instant checkTime = now == null ? Instant.now() : now;
            if (otpExpiresAt != null && checkTime.isAfter(otpExpiresAt)) {
                return RetailRetailerOtpResolution.expiredOtp();
            }
            return RetailRetailerOtpResolution.active(dtlOtp.trim());
        }
        Optional<String> fromMessage = extractOtpFromMessage(message);
        if (fromMessage.isPresent()) {
            return RetailRetailerOtpResolution.active(fromMessage.get());
        }
        if (stubWhenUnset != null && !stubWhenUnset.isBlank()) {
            return RetailRetailerOtpResolution.active(stubWhenUnset.trim());
        }
        return RetailRetailerOtpResolution.notConfigured();
    }

    public static String upsertOtpInMessage(String message, String otpCode) {
        String code = otpCode == null ? "" : otpCode.trim();
        if (code.isEmpty()) {
            return message;
        }
        String base = message == null ? "" : message;
        int idx = base.toUpperCase(Locale.ROOT).indexOf("RETAIL_OTP:");
        if (idx >= 0) {
            int end = base.indexOf('|', idx);
            String prefix = base.substring(0, idx);
            String suffix = end >= 0 ? base.substring(end) : "";
            return prefix + "RETAIL_OTP:" + code + suffix;
        }
        if (base.isBlank()) {
            return "RETAIL_OTP:" + code;
        }
        return base + "|RETAIL_OTP:" + code;
    }

    public static String markVerifiedInMessage(String message) {
        String base = message == null ? "" : message;
        if (isVerifiedInMessage(base)) {
            return base;
        }
        if (base.isBlank()) {
            return "OTP_VERIFIED:Y";
        }
        return base + "|OTP_VERIFIED:Y";
    }

    public static boolean isVerifiedInMessage(String message) {
        if (message == null) {
            return false;
        }
        String upper = message.toUpperCase(Locale.ROOT);
        return upper.contains("OTP_VERIFIED:Y");
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
