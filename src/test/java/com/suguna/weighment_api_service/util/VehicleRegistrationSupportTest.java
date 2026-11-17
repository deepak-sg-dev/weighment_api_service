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
    void displayKeepsErpPlateThatWouldFailWriteValidation() {
        assertEquals("TN-09-AB-1234", VehicleRegistrationSupport.displayOrNull("TN-09-AB-1234"));
        assertEquals("TN38", VehicleRegistrationSupport.displayOrNull("TN38"));
        assertNull(VehicleRegistrationSupport.displayOrNull("-"));
        assertNull(VehicleRegistrationSupport.normalizeOrNull("TN38"));
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
