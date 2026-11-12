package com.suguna.weighment_api_service.integration.oracle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiftingDriverMessageSupportTest {

    @Test
    void upsertDriverMobileAddsToken() {
        String result = LiftingDriverMessageSupport.upsertDriverMobile("RETAIL_OTP:1234", "9876543210");
        assertTrue(result.contains("DRIVER_MOB:9876543210"));
        assertTrue(result.contains("RETAIL_OTP:1234"));
    }

    @Test
    void resolvePrefersColumnOverMessage() {
        assertEquals(
                "6363636363",
                LiftingDriverMessageSupport.resolveDriverMobile("6363636363", "DRIVER_MOB:9999999999"));
    }

    @Test
    void upsertTruncatesWhenMessageWouldExceedOracleLimit() {
        String prefix = "X".repeat(LiftingDriverMessageSupport.MAX_MESSAGE_CHARS - 5);
        String result = LiftingDriverMessageSupport.upsertDriverMobile(prefix, "9876543210");
        assertEquals(LiftingDriverMessageSupport.MAX_MESSAGE_CHARS, result.length());
    }
}
