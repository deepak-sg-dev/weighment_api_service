package com.suguna.weighment_api_service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class VehicleRegistrationSupportTest {

    @Test
    void placeholderDashIsNotAPlate() {
        assertNull(VehicleRegistrationSupport.normalizeOrNull("-"));
        assertNull(VehicleRegistrationSupport.normalizeOrNull(" - "));
        assertNull(VehicleRegistrationSupport.normalizeOrNull("NA"));
    }

    @Test
    void stripsHyphensAndSpaces() {
        assertEquals("TN09AB1234", VehicleRegistrationSupport.normalizeOrNull("TN-09-AB-1234"));
        assertEquals("TN09AB1234", VehicleRegistrationSupport.normalizeOrNull("tn 09 ab 1234"));
    }

    @Test
    void mergeKeepsRealPlateWhenLaterValueIsPlaceholder() {
        assertEquals(
                "TN09AB1234",
                VehicleRegistrationSupport.merge("TN09AB1234", "-"));
        assertEquals(
                "TN09AB1234",
                VehicleRegistrationSupport.merge("-", "TN-09-AB-1234"));
    }
}
