package com.suguna.weighment_api_service.integration.oracle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Mirrors driver mobile in {@code sug_mai_birds_lifting_order.message} ({@code DRIVER_MOB:}); primary store is {@code DRIVER_MOBILE}. */
public final class LiftingDriverMessageSupport {

    /** Oracle {@code SUG_MAI_BIRDS_LIFTING_ORDER.MESSAGE} safe upper bound for VARCHAR2 / SUBSTR reads. */
    static final int MAX_MESSAGE_CHARS = 4000;

    private static final Pattern DRIVER_MOB = Pattern.compile("DRIVER_MOB:([0-9]{10})");

    private LiftingDriverMessageSupport() {}

    public static String readDriverMobile(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        Matcher m = DRIVER_MOB.matcher(message);
        return m.find() ? m.group(1) : null;
    }

    /** Prefer dedicated column when set; otherwise {@code DRIVER_MOB:} token in {@code message}. */
    public static String resolveDriverMobile(String columnValue, String message) {
        if (columnValue != null && !columnValue.isBlank()) {
            return columnValue.trim();
        }
        return readDriverMobile(message);
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
        return truncateMessage(without + "|" + token);
    }

    static String truncateMessage(String message) {
        if (message == null) {
            return null;
        }
        if (message.length() <= MAX_MESSAGE_CHARS) {
            return message;
        }
        return message.substring(0, MAX_MESSAGE_CHARS);
    }
}
