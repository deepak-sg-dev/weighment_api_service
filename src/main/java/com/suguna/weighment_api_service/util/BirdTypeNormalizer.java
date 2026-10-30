package com.suguna.weighment_api_service.util;

import java.util.Locale;

/** Maps {@code sug_mai_birds_lifting_order.ordertype} to API birdType (BROILER, CULL, …). */
public final class BirdTypeNormalizer {

    private BirdTypeNormalizer() {}

    public static String fromOrderType(String ordertype) {
        if (ordertype == null || ordertype.isBlank()) {
            return "BROILER";
        }
        String normalized = ordertype.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("CULL")) {
            return "CULL";
        }
        if (normalized.equals("BROILER")
                || normalized.equals("BROIL")
                || normalized.contains("BROILER")) {
            return "BROILER";
        }
        if (normalized.equals("LAYER")) {
            return "LAYER";
        }
        if (looksLikeStrainName(normalized)) {
            return "BROILER";
        }
        return normalized;
    }

    private static boolean looksLikeStrainName(String upper) {
        if (upper.length() > 20) {
            return true;
        }
        return upper.matches(".*\\d.*");
    }
}
