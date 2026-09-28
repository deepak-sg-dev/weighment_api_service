package com.suguna.weighment_api_service.constants;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum ScheduleStatus {

    ASSIGNED,
    IN_PROGRESS,
    DOWNLOADED,
    COMPLETED,
    CLOSED;

    public static Optional<ScheduleStatus> fromQueryValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        try {
            return Optional.of(ScheduleStatus.valueOf(normalized));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public static Set<ScheduleStatus> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(value -> fromQueryValue(value).orElse(null))
                .filter(status -> status != null)
                .collect(Collectors.toSet());
    }
}
