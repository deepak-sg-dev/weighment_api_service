package com.suguna.weighment_api_service.integration.oracle;

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
    }
}
