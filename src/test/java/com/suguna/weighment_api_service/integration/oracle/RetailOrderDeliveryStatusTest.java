package com.suguna.weighment_api_service.integration.oracle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RetailOrderDeliveryStatusTest {

    @Test
    void toApiStatus_mapsPendingDeliveryToPending() {
        assertEquals("PENDING", RetailOrderDeliveryStatus.toApiStatus(RetailOrderDeliveryStatus.PENDING));
        assertEquals("IN_PROGRESS", RetailOrderDeliveryStatus.toApiStatus(RetailOrderDeliveryStatus.IN_PROGRESS));
        assertEquals("COMPLETED", RetailOrderDeliveryStatus.toApiStatus(RetailOrderDeliveryStatus.COMPLETED));
    }

    @Test
    void resolveForOrder_prefersDelvStatusColumn() {
        assertEquals(
                "IN_PROGRESS",
                RetailOrderDeliveryStatus.resolveForOrder("IN_PROGRESS", true, "Y", "Order Pushed Successfully", null));
        assertEquals(
                "PENDING",
                RetailOrderDeliveryStatus.resolveForOrder(null, false, "Y", "Order Pushed Successfully", null));
    }

    @Test
    void resolveApiStatus_erpPushIsPending() {
        assertEquals(
                "PENDING",
                RetailOrderDeliveryStatus.resolveApiStatus("Y", "Order Pushed Successfully - 495", null));
    }

    @Test
    void fromOrderRow_pendingWhenEmpty() {
        assertEquals(
                RetailOrderDeliveryStatus.PENDING,
                RetailOrderDeliveryStatus.fromOrderRow(null, null));
    }

    @Test
    void fromOrderRow_pendingWhenErpPushOnly() {
        assertEquals(
                RetailOrderDeliveryStatus.PENDING,
                RetailOrderDeliveryStatus.fromOrderRow("Y", "Order Pushed Successfully - 495"));
    }

    @Test
    void fromOrderRow_inProgressWhenDeliveryStartedDateSet() {
        assertEquals(
                RetailOrderDeliveryStatus.IN_PROGRESS,
                RetailOrderDeliveryStatus.fromOrderRow(null, null, java.time.Instant.parse("2026-10-20T10:00:00Z")));
    }

    @Test
    void fromOrderRow_inProgressWhenWeighmentFlag() {
        String message = DeliveryOrderMessageSupport.mergeWeighmentPayload(null, "1001", "{}");
        assertEquals(
                RetailOrderDeliveryStatus.IN_PROGRESS,
                RetailOrderDeliveryStatus.fromOrderRow(null, message));
    }

    @Test
    void aggregate_allComplete() {
        assertEquals(
                "COMPLETED",
                RetailOrderDeliveryStatus.aggregateForLiftingOrder(
                        List.of(RetailOrderDeliveryStatus.COMPLETED, RetailOrderDeliveryStatus.COMPLETED)));
    }

    @Test
    void isStartedOrBeyond_honorsStartDateWhenDelvStatusStillPending() {
        assertTrue(RetailOrderDeliveryStatus.isStartedOrBeyond(
                "PENDING", null, Instant.parse("2026-10-28T05:36:00Z")));
    }

    @Test
    void aggregate_inProgressWhenMixed() {
        assertEquals(
                "IN_PROGRESS",
                RetailOrderDeliveryStatus.aggregateForLiftingOrder(
                        List.of(RetailOrderDeliveryStatus.COMPLETED, RetailOrderDeliveryStatus.PENDING)));
    }
}
