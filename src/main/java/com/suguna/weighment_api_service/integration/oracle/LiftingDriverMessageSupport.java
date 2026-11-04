package com.suguna.weighment_api_service.integration.oracle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Stores driver mobile on {@code sug_mai_birds_lifting_order.message} when no dedicated column exists. */
public final class LiftingDriverMessageSupport {

    private static final Pattern DRIVER_MOB = Pattern.compile("DRIVER_MOB:([0-9]{10})");

    private LiftingDriverMessageSupport() {}

    public static String readDriverMobile(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        Matcher m = DRIVER_MOB.matcher(message);
        return m.find() ? m.group(1) : null;
    }

    public static String upsertDriverMobile(String message, String driverMobile) {
        String base = message == null ? "" : message.trim();
        String without = DRIVER_MOB.matcher(base).replaceAll("").trim();
        if (driverMobile == null || driverMobile.isBlank()) {
            return without.isEmpty() ? null : without;
        }
        String token = "DRIVER_MOB:" + driverMobile.trim();
        if (without.isEmpty()) {
            return token;
        }
        return without + "|" + token;
    }
}
