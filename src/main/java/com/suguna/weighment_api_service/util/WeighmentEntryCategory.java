package com.suguna.weighment_api_service.util;

import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import java.util.Locale;
import java.util.Set;

/**
 * Weighment line category on {@code weighmentEntries[]} (lifting and delivery submit).
 *
 * <ul>
 *   <li>{@link #NORMAL} — standard cage/set
 *   <li>{@link #LAME} / {@link #REPLACEMENT} — bird adjustments ({@code birds_adjusted=Y})
 *   <li>{@link #EMPTY} — empty cage / tare weighment (stored like NORMAL for adjustments; often {@code birdsApplied=0})
 * </ul>
 */
public final class WeighmentEntryCategory {

    public static final String NORMAL = "NORMAL";
    public static final String LAME = "LAME";
    public static final String REPLACEMENT = "REPLACEMENT";
    public static final String EMPTY = "EMPTY";

    private static final Set<String> KNOWN =
            Set.of(NORMAL, LAME, REPLACEMENT, EMPTY);

    private WeighmentEntryCategory() {}

    /** Uppercase token for DB {@code cage_or_set_ref}; default {@link #NORMAL}. */
    public static String normalize(String category) {
        if (category == null || category.isBlank()) {
            return NORMAL;
        }
        return category.trim().toUpperCase(Locale.ROOT);
    }

    /** {@link #LAME} and {@link #REPLACEMENT} only — not {@link #EMPTY}. */
    public static boolean requiresBirdAdjustment(String normalizedCategory) {
        return LAME.equals(normalizedCategory) || REPLACEMENT.equals(normalizedCategory);
    }

    public static void validateIfPresent(Integer sequence, String category) {
        if (category == null || category.isBlank()) {
            return;
        }
        String normalized = normalize(category);
        if (KNOWN.contains(normalized)) {
            return;
        }
        String seq = sequence != null ? String.valueOf(sequence) : "?";
        throw new BusinessValidationException(
                ErrorCodes.VALIDATION_FAILED,
                "Weighment entry sequence "
                        + seq
                        + ": category must be NORMAL, LAME, REPLACEMENT, or EMPTY");
    }
}
