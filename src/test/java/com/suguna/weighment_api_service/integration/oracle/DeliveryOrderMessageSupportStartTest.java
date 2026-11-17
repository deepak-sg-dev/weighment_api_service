package com.suguna.weighment_api_service.integration.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class DeliveryOrderMessageSupportStartTest {

    @Test
    void mergeDeliveryStart_setsStartedSegment() {
        String message = DeliveryOrderMessageSupport.mergeDeliveryStart(
                null, "211409233", Instant.parse("2026-10-20T10:00:00Z"), 12.97, 77.59);
        assertTrue(DeliveryOrderMessageSupport.hasDeliveryStarted(message));
        assertTrue(DeliveryOrderMessageSupport.isDeliveryWeighmentMessage(message));
        assertEquals("211409233", DeliveryOrderMessageSupport.boundOrderId(message));
    }

    @Test
    void mergeDeliveryStart_resetsWhenMessageBoundToAnotherShop() {
        String contaminated =
                "DW_V1|ORDER:999|STARTED:2026-11-16T06:00:00Z|START_LAT:1|START_LON:2";
        Instant started = Instant.parse("2026-11-16T06:58:00Z");
        String message = DeliveryOrderMessageSupport.mergeDeliveryStart(
                contaminated, "211409245", started, 18.434268, 73.851747);
        assertEquals("211409245", DeliveryOrderMessageSupport.boundOrderId(message));
        assertTrue(message.contains("STARTED:2026-11-16T06:58:00Z"));
        assertFalse(message.contains("ORDER:999"));
        assertFalse(message.contains("2026-11-16T06:00:00Z"));
    }
}
