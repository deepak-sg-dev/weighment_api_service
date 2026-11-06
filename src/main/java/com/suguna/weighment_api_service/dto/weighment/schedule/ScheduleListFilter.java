package com.suguna.weighment_api_service.dto.weighment.schedule;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Optional list API filters (intended for mobile QA / UAT). When query params are omitted, server
 * defaults from {@link OracleErpProperties} apply. Product may narrow or remove these params later.
 */
public final class ScheduleListFilter {

    private static final int MAX_SCHEDULE_IDS = 50;

    private final Integer lookbackDaysOverride;
    private final Set<String> scheduleIds;
    /** When true, {@code order_request_date} must fall within lookback (no NULL-date bypass). */
    private final boolean requireOrderRequestDate;

    private ScheduleListFilter(
            Integer lookbackDaysOverride, Set<String> scheduleIds, boolean requireOrderRequestDate) {
        this.lookbackDaysOverride = lookbackDaysOverride;
        this.scheduleIds = scheduleIds == null ? Set.of() : Set.copyOf(scheduleIds);
        this.requireOrderRequestDate = requireOrderRequestDate;
    }

    public static ScheduleListFilter fromQuery(Integer lookbackDays, String scheduleIdsCsv) {
        Integer lookback = lookbackDays;
        Set<String> ids = parseScheduleIds(scheduleIdsCsv);
        if (lookback == null && ids.isEmpty()) {
            return none();
        }
        boolean requireDate = lookback != null;
        return new ScheduleListFilter(lookback, ids, requireDate);
    }

    public static ScheduleListFilter none() {
        return new ScheduleListFilter(null, Set.of(), false);
    }

    public boolean restrictsScheduleIds() {
        return !scheduleIds.isEmpty();
    }

    public Set<String> scheduleIds() {
        return scheduleIds;
    }

    /** {@code 0} = no order_request_date lookback clause. */
    public int effectiveLookbackDays(OracleErpProperties erpProperties) {
        if (lookbackDaysOverride != null) {
            return lookbackDaysOverride;
        }
        return erpProperties.getScheduleListLookbackDays();
    }

    public boolean requireOrderRequestDate() {
        return requireOrderRequestDate;
    }

    private static Set<String> parseScheduleIds(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        LinkedHashSet<String> ids = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ScheduleIdSupport::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (ids.size() > MAX_SCHEDULE_IDS) {
            throw new IllegalArgumentException(
                    "scheduleIds supports at most " + MAX_SCHEDULE_IDS + " ids");
        }
        return ids;
    }
}
