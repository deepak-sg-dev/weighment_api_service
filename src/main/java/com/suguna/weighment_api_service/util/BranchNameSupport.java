package com.suguna.weighment_api_service.util;

import java.util.regex.Pattern;

/** Derives mobile-friendly branch labels from ERP-style names (e.g. {@code M47:INDAPUR BROILER BRANCH-MH}). */
public final class BranchNameSupport {

    private static final Pattern BROILER_BRANCH_SUFFIX =
            Pattern.compile("\\s+BROILER\\s+BRANCH(-[A-Z]{2})?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern BRANCH_STATE_SUFFIX =
            Pattern.compile("\\s+BRANCH(-[A-Z]{2})?$", Pattern.CASE_INSENSITIVE);

    private BranchNameSupport() {}

    /**
     * Short display name: text after {@code CODE:}, with common {@code BROILER BRANCH-XX} suffix removed.
     * Falls back to trimmed full name when parsing yields nothing.
     */
    public static String deriveShortName(String fullBranchName) {
        if (fullBranchName == null || fullBranchName.isBlank()) {
            return null;
        }
        String working = fullBranchName.trim();
        int colon = working.indexOf(':');
        if (colon >= 0 && colon < working.length() - 1) {
            working = working.substring(colon + 1).trim();
        }
        working = stripBranchSuffixes(working);
        if (working.isBlank()) {
            return fullBranchName.trim();
        }
        return working;
    }

    private static String stripBranchSuffixes(String value) {
        String result = BROILER_BRANCH_SUFFIX.matcher(value).replaceFirst("").trim();
        result = BRANCH_STATE_SUFFIX.matcher(result).replaceFirst("").trim();
        return result;
    }
}
