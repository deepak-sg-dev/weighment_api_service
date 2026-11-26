package com.suguna.weighment_api_service.util;

import java.util.Locale;

/**
 * ERP {@code SUG_MAI_LIFTING_VEHICLE_MST.VEHICLE_TYPE} / weighment {@code VEHICLE_TYPE}: {@code P} primary,
 * {@code S} secondary. APIs expose {@code PRIMARY} / {@code SECONDARY}.
 */
public final class VehicleTypeSupport {

    public static final String CODE_PRIMARY = "P";
    public static final String CODE_SECONDARY = "S";
    public static final String LABEL_PRIMARY = "PRIMARY";
    public static final String LABEL_SECONDARY = "SECONDARY";

    private VehicleTypeSupport() {}

    /** Persist as {@code P} or {@code S}. Accepts P/S or PRIMARY/SECONDARY. */
    public static String toStorageCode(String raw) {
        String key = compact(raw);
        if (key == null) {
            return null;
        }
        if (CODE_PRIMARY.equals(key) || LABEL_PRIMARY.equals(key) || "PRIMARYVEHICLE".equals(key)) {
            return CODE_PRIMARY;
        }
        if (CODE_SECONDARY.equals(key) || LABEL_SECONDARY.equals(key) || "SECONDARYVEHICLE".equals(key)) {
            return CODE_SECONDARY;
        }
        return null;
    }

    /** JSON/API value: PRIMARY or SECONDARY. */
    public static String toApiLabel(String raw) {
        String code = toStorageCode(raw);
        if (CODE_PRIMARY.equals(code)) {
            return LABEL_PRIMARY;
        }
        if (CODE_SECONDARY.equals(code)) {
            return LABEL_SECONDARY;
        }
        return null;
    }

    private static String compact(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String compact = raw.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        return compact.isEmpty() ? null : compact;
    }
}
