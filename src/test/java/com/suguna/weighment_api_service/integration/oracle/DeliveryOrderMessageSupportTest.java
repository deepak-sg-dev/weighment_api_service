package com.suguna.weighment_api_service.integration.oracle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeliveryOrderMessageSupportTest {

    @Test
    void isComplete_whenAllFlagsPresent() {
        String message =
                DeliveryOrderMessageSupport.PREFIX
                        + "ORDER:1001|FLAGS:WEIGHMENT,SHOP_PHOTO,CUSTOMER_ACK";
        assertTrue(DeliveryOrderMessageSupport.isComplete(message));
    }

    @Test
    void isComplete_falseUntilCustomerAck() {
        String base = DeliveryOrderMessageSupport.mergeWeighmentPayload(null, "1001", "{\"ok\":true}");
        base = DeliveryOrderMessageSupport.mergeShopPhoto(base, "1001", "shop-key");
        assertFalse(DeliveryOrderMessageSupport.isComplete(base));
    }

    @Test
    void mergeWeighment_setsWeighmentFlag() {
        String message = DeliveryOrderMessageSupport.mergeWeighmentPayload(null, "1001", "{}");
        assertTrue(DeliveryOrderMessageSupport.hasWeighmentReceived(message));
    }
}
