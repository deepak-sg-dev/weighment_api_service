package com.suguna.weighment_api_service.dto.weighment.schedule;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Parses {@code GET /weighment/schedules?status=} for mobile list tabs. */
public final class ScheduleListStatusFilter {

    private static final Set<ScheduleStatus> DEFAULT_OPEN_STATUSES =
            EnumSet.of(ScheduleStatus.ASSIGNED, ScheduleStatus.DOWNLOADED, ScheduleStatus.IN_PROGRESS);

    private final boolean includeAll;
    private final Set<ScheduleStatus> statuses;

    private ScheduleListStatusFilter(boolean includeAll, Set<ScheduleStatus> statuses) {
        this.includeAll = includeAll;
        this.statuses = statuses == null ? Set.of() : Set.copyOf(statuses);
    }

    public static ScheduleListStatusFilter fromQuery(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ScheduleListStatusFilter(false, DEFAULT_OPEN_STATUSES);
        }
        if (isAllToken(raw.trim())) {
            return all();
        }
        Set<ScheduleStatus> parsed = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ScheduleListStatusFilter::mapToken)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        if (parsed.isEmpty()) {
            return invalid();
        }
        return new ScheduleListStatusFilter(false, parsed);
    }

    public static ScheduleListStatusFilter all() {
        return new ScheduleListStatusFilter(true, Set.of());
    }

    /** Sentinel when query contained unknown tokens. */
    public static ScheduleListStatusFilter invalid() {
        return new ScheduleListStatusFilter(false, Set.of());
    }

    public boolean isIncludeAll() {
        return includeAll;
    }

    public Set<ScheduleStatus> statuses() {
        return statuses;
    }

    public boolean isValid() {
        return includeAll || !statuses.isEmpty();
    }

    private static boolean isAllToken(String token) {
        String normalized = normalizeToken(token);
        return "ALL".equals(normalized) || "*".equals(normalized);
    }

    private static Optional<ScheduleStatus> mapToken(String raw) {
        if (isAllToken(raw)) {
            return Optional.empty();
        }
        String normalized = normalizeToken(raw);
        return switch (normalized) {
            case "PENDING", "ASSIGNED" -> Optional.of(ScheduleStatus.ASSIGNED);
            case "DOWNLOADED" -> Optional.of(ScheduleStatus.DOWNLOADED);
            case "IN_PROGRESS", "INPROGRESS" -> Optional.of(ScheduleStatus.IN_PROGRESS);
            case "COMPLETED", "COMPLETE" -> Optional.of(ScheduleStatus.COMPLETED);
            default -> ScheduleStatus.fromQueryValue(raw);
        };
    }

    private static String normalizeToken(String raw) {
        return raw.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
