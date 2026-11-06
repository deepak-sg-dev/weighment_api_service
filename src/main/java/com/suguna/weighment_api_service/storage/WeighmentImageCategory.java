package com.suguna.weighment_api_service.storage;

import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.exception.ApiException;
import java.util.Locale;

/**
 * API-facing image category. Maps to storage {@link WeighmentImageDomain} + {@link WeighmentImageKind}.
 *
 * <ul>
 *   <li>VEHICLE, COMPLETION — under {@code lifting/.../}
 *   <li>SHOP, SIGNATURE, CUSTOMER — under {@code delivery/.../} (require {@code orderId})
 *   <li>OTHER — general/misc; folder follows optional {@code domain} (defaults to lifting)
 * </ul>
 */
public enum WeighmentImageCategory {
    VEHICLE(WeighmentImageDomain.LIFTING, WeighmentImageKind.VEHICLE, false),
    COMPLETION(WeighmentImageDomain.LIFTING, WeighmentImageKind.COMPLETION, false),
    SHOP(WeighmentImageDomain.DELIVERY, WeighmentImageKind.SHOP, true),
    SIGNATURE(WeighmentImageDomain.DELIVERY, WeighmentImageKind.SIGNATURE, true),
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
        try {
            return WeighmentImageCategory.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    ErrorCode.BAD_REQUEST,
                    "Unsupported category: "
                            + raw
                            + ". Use VEHICLE, COMPLETION, SHOP, SIGNATURE, CUSTOMER, or OTHER.");
        }
    }
}
