package com.suguna.weighment_api_service.util;

public final class WeighmentSubmitKeys {

    private WeighmentSubmitKeys() {}

    public static String normalizeScheduleId(String scheduleId) {
        return ScheduleIdSupport.normalize(scheduleId);
    }

    /** Stable key for DTL rows and duplicate detection (per submission API v1). */
    public static String transactionKey(String scheduleId) {
        return normalizeScheduleId(scheduleId);
    }
}
