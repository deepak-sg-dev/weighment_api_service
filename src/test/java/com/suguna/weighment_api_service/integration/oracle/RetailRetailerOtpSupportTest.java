package com.suguna.weighment_api_service.integration.oracle;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetailRetailerOtpSupportTest {

    private static final Instant NOW = Instant.parse("2026-10-28T10:00:00Z");

    @Test
    void prefersDtlColumnWhenEnabled() {
        var resolution = RetailRetailerOtpSupport.resolve(true, "1234", null, "RETAIL_OTP:9999", NOW);
        assertTrue(resolution.configured());
        assertEquals("1234", resolution.expectedCode());
    }

    @Test
    void fallsBackToMessageWhenDtlColumnBlank() {
        var resolution = RetailRetailerOtpSupport.resolve(true, " ", null, "x|RETAIL_OTP:5678|y", NOW);
        assertEquals("5678", resolution.expectedCode());
    }

    @Test
    void expiredWhenPastOtpExpiresAt() {
        var resolution = RetailRetailerOtpSupport.resolve(
                true, "1234", Instant.parse("2026-10-28T09:00:00Z"), null, NOW);
        assertTrue(resolution.expired());
    }

    @Test
    void notConfiguredWhenNoOtpAnywhere() {
        var resolution = RetailRetailerOtpSupport.resolve(false, null, null, "no otp here", NOW);
        assertFalse(resolution.configured());
    }

    @Test
    void stubUsedWhenNoDtlOrMessageOtp() {
        var resolution = RetailRetailerOtpSupport.resolve(false, null, null, "no otp here", NOW, "1234");
        assertTrue(resolution.configured());
        assertEquals("1234", resolution.expectedCode());
    }

    @Test
    void stubIgnoredWhenDtlOtpPresent() {
        var resolution = RetailRetailerOtpSupport.resolve(true, "5678", null, null, NOW, "1234");
        assertEquals("5678", resolution.expectedCode());
    }
}
