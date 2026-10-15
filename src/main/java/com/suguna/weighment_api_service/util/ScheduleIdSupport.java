package com.suguna.weighment_api_service.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * ERP lifting schedules use either a plain indent ({@code 4747161}) or a composite id
 * ({@code 211342644/1.1}). Composite values must not be sent as extra URL path segments.
 */
public final class ScheduleIdSupport {

    /** Match {@code sug_mai_birds_lifting_order.erpindentid} to API schedule id (full or indent prefix). */
    public static final String LIFTING_ORDER_ERP_INDENT_MATCH =
            """
             (TO_CHAR(o.erpindentid) = TRIM(?)
              OR TO_CHAR(o.erpindentid) = REGEXP_SUBSTR(TRIM(?), '^[^/]+'))
            """;

    /** Match {@code sug_mai_birds_weighment.schedule_no} to API schedule id (full or indent prefix). */
    public static final String WEIGHMENT_SCHEDULE_NO_MATCH =
            """
             (TO_CHAR(w.schedule_no) = TRIM(?)
              OR TO_CHAR(w.schedule_no) = REGEXP_SUBSTR(TRIM(?), '^[^/]+'))
            """;

    private ScheduleIdSupport() {}

    public static String normalize(String scheduleId) {
        if (scheduleId == null) {
            return null;
        }
        String trimmed = scheduleId.trim();
        if (trimmed.contains("%")) {
            try {
                trimmed = URLDecoder.decode(trimmed, StandardCharsets.UTF_8).trim();
            } catch (IllegalArgumentException ignored) {
                // keep trimmed as-is
            }
        }
        return trimmed;
    }

    /** Indent portion before the first {@code /} (e.g. {@code 211342644/1.1} → {@code 211342644}). */
    public static String erpIndentPart(String scheduleId) {
        String normalized = normalize(scheduleId);
        if (normalized == null || normalized.isEmpty()) {
            return normalized;
        }
        int slash = normalized.indexOf('/');
        return slash < 0 ? normalized : normalized.substring(0, slash);
    }

    public static long parseErpIndentAsLong(String scheduleId) {
        String part = erpIndentPart(scheduleId);
        if (part == null || part.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(part.trim());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    /** Two JDBC bind values for {@link #LIFTING_ORDER_ERP_INDENT_MATCH} / {@link #WEIGHMENT_SCHEDULE_NO_MATCH}. */
    public static Object[] scheduleIdBinds(String scheduleId) {
        String normalized = normalize(scheduleId);
        return new Object[] {normalized, normalized};
    }
}
