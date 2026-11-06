package com.suguna.weighment_api_service.storage;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.exception.ApiException;
import java.util.Locale;

/**
 * API-facing image category. Maps to storage {@link WeighmentImageDomain} + {@link WeighmentImageKind}.
 *
 * <ul>
 *   <li>Lifting: {@code VEHICLE}, {@code COMPLETION}, {@code FARMER_SIGN}, {@code LIFT_SUP_SIGN}, {@code TRADER_SIGN} —
 *       under {@code lifting/{retail|wholesale}/{scheduleId}/} (schedule id in file name). {@code TRADER_SIGN} is
 *       accepted only for WHOLESALE schedules. Older names are still accepted by {@link #parse(String)}.
 *   <li>SHOP, CUST_SIGN, DEL_SUP_SIGN, CUSTOMER — under {@code delivery/.../}
 *       (require {@code orderId} in file name). Aliases: SIGNATURE, CUSTOMER_SIGNATURE, DELIVERY_SUPERVISOR_SIGNATURE.
 *   <li>OTHER — general/misc; folder follows optional {@code domain} (defaults to lifting)
 * </ul>
 */
public enum WeighmentImageCategory {
    VEHICLE(WeighmentImageDomain.LIFTING, WeighmentImageKind.VEHICLE, false),
    COMPLETION(WeighmentImageDomain.LIFTING, WeighmentImageKind.COMPLETION, false),
    /** Farmer signature at lifting (aliases: FARMER_SIGNATURE, FARM_SIGN). */
    FARMER_SIGN(WeighmentImageDomain.LIFTING, WeighmentImageKind.FARMER_SIGNATURE, false),
    /** Lifting supervisor signature (aliases: LIFTING_SUPERVISOR_SIGNATURE). */
    LIFT_SUP_SIGN(WeighmentImageDomain.LIFTING, WeighmentImageKind.LIFTING_SUPERVISOR_SIGNATURE, false),
    /** Trader signature at lifting — WHOLESALE only (aliases: TRADER_SIGNATURE, TR_SIGN). */
    TRADER_SIGN(WeighmentImageDomain.LIFTING, WeighmentImageKind.TRADER_SIGNATURE, false),
    SHOP(WeighmentImageDomain.DELIVERY, WeighmentImageKind.SHOP, true),
    /** Customer / retailer signature at delivery. */
    CUST_SIGN(WeighmentImageDomain.DELIVERY, WeighmentImageKind.SIGNATURE, true),
    /** Supervisor signature at delivery (after customer). */
    DEL_SUP_SIGN(WeighmentImageDomain.DELIVERY, WeighmentImageKind.DELIVERY_SUPERVISOR_SIGNATURE, true),
    CUSTOMER(WeighmentImageDomain.DELIVERY, WeighmentImageKind.CUSTOMER, true),
    OTHER(WeighmentImageDomain.LIFTING, WeighmentImageKind.OTHER, false);

    private final WeighmentImageDomain defaultDomain;
    private final WeighmentImageKind kind;
    private final boolean orderIdRequired;

    WeighmentImageCategory(
            WeighmentImageDomain defaultDomain, WeighmentImageKind kind, boolean orderIdRequired) {
        this.defaultDomain = defaultDomain;
        this.kind = kind;
        this.orderIdRequired = orderIdRequired;
    }

    public WeighmentImageDomain defaultDomain() {
        return defaultDomain;
    }

    public WeighmentImageKind kind() {
        return kind;
    }

    public boolean isOrderIdRequired() {
        return orderIdRequired;
    }

    public static WeighmentImageCategory parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "category is required");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        if ("GENERAL".equals(normalized)) {
            normalized = "OTHER";
        }
        normalized = switch (normalized) {
            case "VH" -> "VEHICLE";
            case "CP" -> "COMPLETION";
            case "FARM_SIGN" -> "FARMER_SIGN";
            case "FARMER_SIGNATURE" -> "FARMER_SIGN";
            case "LIFTING_SUPERVISOR_SIGNATURE" -> "LIFT_SUP_SIGN";
            case "TR_SIGN" -> "TRADER_SIGN";
            case "TRADER_SIGNATURE" -> "TRADER_SIGN";
            case "CUSTOMER_SIGNATURE", "SIGNATURE" -> "CUST_SIGN";
            case "DELIVERY_SUPERVISOR_SIGNATURE" -> "DEL_SUP_SIGN";
            default -> normalized;
        };
        try {
            return WeighmentImageCategory.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    ErrorCode.BAD_REQUEST,
                    "Unsupported category: "
                            + raw
                            + ". Supported: "
                            + String.join(
                                    ", ",
                                    java.util.Arrays.stream(values())
                                            .map(Enum::name)
                                            .toList())
                            + " (aliases: VH→VEHICLE, CP→COMPLETION, FARM_SIGN|FARMER_SIGNATURE→FARMER_SIGN, "
                            + "LIFTING_SUPERVISOR_SIGNATURE→LIFT_SUP_SIGN, TR_SIGN|TRADER_SIGNATURE→TRADER_SIGN; "
                            + "SIGNATURE, CUSTOMER_SIGNATURE → CUST_SIGN; "
                            + "DELIVERY_SUPERVISOR_SIGNATURE → DEL_SUP_SIGN).");
        }
    }
}
