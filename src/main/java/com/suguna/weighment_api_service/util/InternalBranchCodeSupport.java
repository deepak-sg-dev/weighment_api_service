package com.suguna.weighment_api_service.util;

/**
 * ERP org labels are often {@code M47:INDAPUR BROILER BRANCH-MH} or {@code MR1:PUNE RETAIL}.
 * {@code SUG_MAI_BIRDS_WEIGHMENT.BRANCH_CODE} stores the internal code before {@code :}.
 */
public final class InternalBranchCodeSupport {

    private InternalBranchCodeSupport() {}

    public static String fromLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        int colon = trimmed.indexOf(':');
        if (colon <= 0) {
            return trimmed;
        }
        String code = trimmed.substring(0, colon).trim();
        return code.isEmpty() ? trimmed : code;
    }
}
