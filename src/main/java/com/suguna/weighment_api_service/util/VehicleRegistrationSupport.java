package com.suguna.weighment_api_service.util;

import java.util.Locale;
import java.util.Set;

/**
 * Normalizes vehicle registration for Oracle {@code VEHICLENO} ({@code VARCHAR2(15)}).
 * ERP often stores {@code -} as empty; mobile may send the plate under {@code vehicleNo}.
 */
public final class VehicleRegistrationSupport {

    public static final int ORACLE_VEHICLENO_MAX = 15;
    public static final int MIN_CHARS = 6;
    public static final int MAX_CHARS = 13;

    private static final Set<String> PLACEHOLDERS = Set.of("NA", "NONE", "NIL", "NULL", "0");

    private VehicleRegistrationSupport() {}

    /**
     * Prefer a real plate over ERP/mobile placeholders. Later real values win; placeholders never
     * overwrite a real plate.
     */
    public static String merge(String current, String incoming) {
        String in = normalizeOrNull(incoming);
        if (in != null) {
            return in;
        }
        return normalizeOrNull(current);
    }

    /** Compact uppercase A–Z/0–9, or {@code null} when blank / placeholder / too short. */
    public static String normalizeOrNull(String raw) {
        if (raw == null) {
            return null;
        }
        String compact = raw.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (compact.isEmpty() || PLACEHOLDERS.contains(compact)) {
            return null;
        }
        if (compact.length() > ORACLE_VEHICLENO_MAX) {
            compact = compact.substring(0, ORACLE_VEHICLENO_MAX);
        }
        if (compact.length() < MIN_CHARS) {
            return null;
        }
        if (compact.length() > MAX_CHARS) {
            compact = compact.substring(0, MAX_CHARS);
        }
        return compact;
    }

    public static boolean isUsable(String raw) {
        return normalizeOrNull(raw) != null;
    }
}
