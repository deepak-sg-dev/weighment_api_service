package com.suguna.weighment_api_service.integration.oracle;

import java.time.Instant;
import java.util.Collection;
import java.util.Locale;

/** Maps {@code sug_mai_ntail_order} columns to mobile delivery row / schedule aggregate status. */
public final class RetailOrderDeliveryStatus {

    static final String COMPLETED = "COMPLETED";
    static final String IN_PROGRESS = "IN_PROGRESS";
    static final String PENDING = "PENDING_DELIVERY";

    private RetailOrderDeliveryStatus() {}

    public static String fromOrderRow(String uploadedFlag, String message) {
        return fromOrderRow(uploadedFlag, message, null);
    }

    public static String fromOrderRow(String uploadedFlag, String message, Instant deliveryStartedAt) {
        if (isDeliveryComplete(uploadedFlag, message)) {
            return COMPLETED;
        }
        if (deliveryStartedAt != null) {
            return IN_PROGRESS;
        }
        if (DeliveryOrderMessageSupport.hasDeliveryStarted(message)) {
            return IN_PROGRESS;
        }
        if (message != null && message.toUpperCase(Locale.ROOT).startsWith("ARRIVED")) {
            return IN_PROGRESS;
        }
        if (DeliveryOrderMessageSupport.isDeliveryWeighmentMessage(message)) {
            if (DeliveryOrderMessageSupport.hasWeighmentReceived(message)
                    || DeliveryOrderMessageSupport.hasShopPhoto(message)
                    || DeliveryOrderMessageSupport.hasCustomerAck(message)) {
                return IN_PROGRESS;
            }
        }
        if (message != null && message.toUpperCase(Locale.ROOT).contains("IDEMPOTENCY:")) {
            return IN_PROGRESS;
        }
        return PENDING;
    }

    /** API values: {@code PENDING}, {@code IN_PROGRESS}, {@code COMPLETED}. */
    public static String toApiStatus(String internalStatus) {
        if (internalStatus == null || internalStatus.isBlank()) {
            return "PENDING";
        }
        String normalized = internalStatus.trim().toUpperCase(Locale.ROOT);
        if (PENDING.equals(normalized) || normalized.contains("PENDING")) {
            return "PENDING";
        }
        if (COMPLETED.equals(normalized)) {
            return "COMPLETED";
        }
        if (IN_PROGRESS.equals(normalized) || normalized.contains("PROGRESS") || normalized.contains("ARRIVED")) {
            return "IN_PROGRESS";
        }
        return "PENDING";
    }

    public static String resolveApiStatus(String uploadedFlag, String message, Instant deliveryStartedAt) {
        return toApiStatus(fromOrderRow(uploadedFlag, message, deliveryStartedAt));
    }

    /**
     * Prefer {@code DELV_STATUS} when column mode is enabled; otherwise infer from legacy columns.
     */
    public static String resolveForOrder(
            String delvStatusFromDb,
            boolean statusColumnEnabled,
            String uploadedFlag,
            String message,
            Instant deliveryStartedAt) {
        if (statusColumnEnabled && delvStatusFromDb != null && !delvStatusFromDb.isBlank()) {
            return toApiStatus(delvStatusFromDb);
        }
        return resolveApiStatus(uploadedFlag, message, deliveryStartedAt);
    }

    public static boolean isStartedOrBeyond(String delvStatusFromDb, String message, Instant deliveryStartedAt) {
        if (delvStatusFromDb != null && !delvStatusFromDb.isBlank()) {
            String api = toApiStatus(delvStatusFromDb);
            if ("COMPLETED".equals(api) || IN_PROGRESS.equals(api)) {
                return true;
            }
            // Column still PENDING: honor DELV_START_DT / DW_V1 message (start before status backfill).
        }
        return isDeliveryStarted(message, deliveryStartedAt);
    }

    /** True when the shop delivery workflow is finished (not ERP ntail upload-only). */
    public static boolean isDeliveryComplete(String uploadedFlag, String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        if (DeliveryOrderMessageSupport.isDeliveryWeighmentMessage(message)) {
            return DeliveryOrderMessageSupport.isComplete(message);
        }
        String upper = message.toUpperCase(Locale.ROOT);
        if (upper.contains("IDEMPOTENCY:") && upper.contains("COMPLETED:")) {
            return true;
        }
        if ("Y".equalsIgnoreCase(uploadedFlag) && upper.contains("OTP_VERIFIED")) {
            return true;
        }
        return false;
    }

    public static boolean isDeliveryStarted(String message, Instant deliveryStartedAt) {
        if (deliveryStartedAt != null) {
            return true;
        }
        return DeliveryOrderMessageSupport.hasDeliveryStarted(message);
    }

    /** Values for {@code sug_mai_birds_lifting_order.delivery_status}. */
    static String aggregateForLiftingOrder(Collection<String> orderStatuses) {
        if (orderStatuses == null || orderStatuses.isEmpty()) {
            return "PENDING";
        }
        boolean allComplete = orderStatuses.stream().allMatch(RetailOrderDeliveryStatus::isCompleted);
        if (allComplete) {
            return "COMPLETED";
        }
        boolean anyProgress = orderStatuses.stream().anyMatch(RetailOrderDeliveryStatus::isInProgressOrComplete);
        return anyProgress ? "IN_PROGRESS" : "PENDING";
    }

    private static boolean isCompleted(String status) {
        return status != null && COMPLETED.equalsIgnoreCase(status.trim());
    }

    private static boolean isInProgressOrComplete(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return COMPLETED.equals(normalized)
                || IN_PROGRESS.equals(normalized)
                || normalized.contains("ARRIVED")
                || normalized.contains("PROGRESS");
    }
}
