package com.suguna.weighment_api_service.integration.oracle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Per-shop delivery vehicle on {@code sug_mai_retail_order_dtl.message} when DTL columns are not enabled. */
public final class RetailDeliveryVehicleMessageSupport {

    static final int MAX_MESSAGE_CHARS = 4000;

    private static final Pattern DELV_VEH = Pattern.compile("DELV_VEH:([A-Z0-9]{6,13})");
    private static final Pattern DELV_DRIVER = Pattern.compile("DELV_DRIVER:([^|]+)");
    private static final Pattern DELV_DRIVER_MOB = Pattern.compile("DELV_DRIVER_MOB:([0-9]{10})");

    private RetailDeliveryVehicleMessageSupport() {}

    public static String readVehicleNo(String message) {
        return readGroup(DELV_VEH, message, 1);
    }

    public static String readDriverName(String message) {
        return readGroup(DELV_DRIVER, message, 1);
    }

    public static String readDriverMobile(String message) {
        return readGroup(DELV_DRIVER_MOB, message, 1);
    }

    public static String upsertAll(String message, String vehicleNo, String driverName, String driverMobile) {
        String base = message == null ? "" : message.trim();
        base = DELV_VEH.matcher(base).replaceAll("").trim();
        base = DELV_DRIVER.matcher(base).replaceAll("").trim();
        base = DELV_DRIVER_MOB.matcher(base).replaceAll("").trim();
        base = stripOrphanPipes(base);
        StringBuilder out = new StringBuilder(base);
        appendToken(out, "DELV_VEH:" + vehicleNo.trim().toUpperCase());
        appendToken(out, "DELV_DRIVER:" + driverName.trim());
        appendToken(out, "DELV_DRIVER_MOB:" + driverMobile.trim());
        return truncate(out.toString());
    }

    private static void appendToken(StringBuilder base, String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        if (base.length() > 0) {
            base.append('|');
        }
        base.append(token);
    }

    private static String stripOrphanPipes(String base) {
        while (base.startsWith("|")) {
            base = base.substring(1).trim();
        }
        while (base.endsWith("|")) {
            base = base.substring(0, base.length() - 1).trim();
        }
        return base.replaceAll("\\|{2,}", "|");
    }

    private static String readGroup(Pattern pattern, String message, int group) {
        if (message == null || message.isBlank()) {
            return null;
        }
        Matcher m = pattern.matcher(message);
        return m.find() ? m.group(group).trim() : null;
    }

    private static String truncate(String message) {
        if (message == null || message.length() <= MAX_MESSAGE_CHARS) {
            return message;
        }
        return message.substring(0, MAX_MESSAGE_CHARS);
    }
}
