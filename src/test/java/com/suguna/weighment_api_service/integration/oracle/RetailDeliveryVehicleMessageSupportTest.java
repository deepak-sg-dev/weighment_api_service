package com.suguna.weighment_api_service.integration.oracle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetailDeliveryVehicleMessageSupportTest {

    @Test
    void upsertAddsTokensWithoutBreakingExistingMessage() {
        String result = RetailDeliveryVehicleMessageSupport.upsertAll(
                "DW_V1|ORDER:211409261|FLAGS:", "TN98D2333", "Murugan", "9876543210");
        assertTrue(result.contains("DW_V1|ORDER:211409261|FLAGS:"));
        assertTrue(result.contains("DELV_VEH:TN98D2333"));
        assertTrue(result.contains("DELV_DRIVER:Murugan"));
        assertTrue(result.contains("DELV_DRIVER_MOB:9876543210"));
    }

    @Test
    void readBackAfterUpsert() {
        String stored = RetailDeliveryVehicleMessageSupport.upsertAll(null, "KA01AB1234", "Ravi", "9123456789");
        assertEquals("KA01AB1234", RetailDeliveryVehicleMessageSupport.readVehicleNo(stored));
        assertEquals("Ravi", RetailDeliveryVehicleMessageSupport.readDriverName(stored));
        assertEquals("9123456789", RetailDeliveryVehicleMessageSupport.readDriverMobile(stored));
    }
}
