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

    /**
     * Index-friendly weighment lookup. Bind {@link #erpIndentBind(String)} once. Prefer this on submit
     * paths; {@link #WEIGHMENT_SCHEDULE_NO_MATCH} cannot use an index on {@code schedule_no}.
     */
    public static final String WEIGHMENT_SCHEDULE_NO_EQ = " w.schedule_no = ? ";

    /**
     * Index-friendly lifting indent lookup. Bind {@link #erpIndentBind(String)} once.
     */
    /**
     * {@code erpindentid} is VARCHAR2. Bind {@link #erpIndentBind(String)} (number) via {@code TO_CHAR}
     * so Oracle does not {@code TO_NUMBER} every indent (ORA-01722 on composite / non-numeric ids).
     */
    public static final String LIFTING_ORDER_ERP_INDENT_EQ = " o.erpindentid = TO_CHAR(?) ";

    /** Farm lifting header only — delivery complete inserts extra rows with {@code source = DELIVERY}. */
    public static final String WEIGHMENT_EXCLUDE_DELIVERY_SOURCE =
            " AND NVL(UPPER(TRIM(w.source)), 'X') <> 'DELIVERY' ";

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

    /** Numeric indent for {@link #WEIGHMENT_SCHEDULE_NO_EQ} / {@link #LIFTING_ORDER_ERP_INDENT_EQ}. */
    public static Long erpIndentBind(String scheduleId) {
        long indent = parseErpIndentAsLong(scheduleId);
        return indent == 0L ? null : indent;
    }
}
