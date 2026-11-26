package com.suguna.weighment_api_service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class VehicleTypeSupportTest {

    @Test
    void pIsPrimary() {
        assertEquals("P", VehicleTypeSupport.toStorageCode("P"));
        assertEquals("PRIMARY", VehicleTypeSupport.toApiLabel("P"));
        assertEquals("PRIMARY", VehicleTypeSupport.toApiLabel("PRIMARY"));
    }

    @Test
    void sIsSecondary() {
        assertEquals("S", VehicleTypeSupport.toStorageCode("s"));
        assertEquals("SECONDARY", VehicleTypeSupport.toApiLabel("S"));
    }

    @Test
    void blankAndUnknownAreNull() {
        assertNull(VehicleTypeSupport.toStorageCode(null));
        assertNull(VehicleTypeSupport.toStorageCode(" "));
        assertNull(VehicleTypeSupport.toStorageCode("X"));
        assertNull(VehicleTypeSupport.toApiLabel("X"));
    }
}
