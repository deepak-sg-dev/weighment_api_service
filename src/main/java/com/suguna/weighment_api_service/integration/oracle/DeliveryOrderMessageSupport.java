package com.suguna.weighment_api_service.integration.oracle;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses and updates {@code sug_mai_ntail_order.message} for retail delivery weighment v1. */
final class DeliveryOrderMessageSupport {

    static final String PREFIX = "DW_V1|";
    static final String FLAG_WEIGHMENT = "WEIGHMENT";
    static final String FLAG_SHOP_PHOTO = "SHOP_PHOTO";
    static final String FLAG_CUSTOMER_ACK = "CUSTOMER_ACK";

    private static final Pattern FLAGS_PATTERN = Pattern.compile("\\|FLAGS:([^|]*)");
    private static final Pattern ORDER_PATTERN = Pattern.compile("\\|ORDER:([^|]*)");
    private static final Pattern SEGMENT_PATTERN = Pattern.compile("\\|([A-Z_]+):([^|]*)");

    private DeliveryOrderMessageSupport() {}

    static boolean isDeliveryWeighmentMessage(String message) {
        return message != null && message.startsWith(PREFIX);
    }

    static boolean hasDeliveryStarted(String message) {
        return message != null
                && isDeliveryWeighmentMessage(message)
                && !segmentValue(message, "STARTED").isBlank();
    }

    static Instant parseDeliveryStartedAt(String message) {
        String raw = segmentValue(message, "STARTED");
        if (raw.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(raw);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    static String mergeDeliveryStart(
            String existing, String orderId, java.time.Instant startedAt, double latitude, double longitude) {
        String base = normalizeBase(existing, orderId);
        String startedToken =
                startedAt == null ? "" : startedAt.toString();
        base = upsertSegment(base, "STARTED", startedToken);
        base = upsertSegment(base, "START_LAT", String.valueOf(latitude));
        base = upsertSegment(base, "START_LON", String.valueOf(longitude));
        return base;
    }

    static boolean hasWeighmentReceived(String message) {
        return flags(message).contains(FLAG_WEIGHMENT);
    }

    static boolean hasShopPhoto(String message) {
        return flags(message).contains(FLAG_SHOP_PHOTO);
    }

    static boolean hasCustomerAck(String message) {
        return flags(message).contains(FLAG_CUSTOMER_ACK);
    }

    static boolean isComplete(String message) {
        Set<String> flags = flags(message);
        return flags.contains(FLAG_WEIGHMENT)
                && flags.contains(FLAG_SHOP_PHOTO)
                && flags.contains(FLAG_CUSTOMER_ACK);
    }

    static String boundOrderId(String message) {
        if (message == null) {
            return null;
        }
        Matcher m = ORDER_PATTERN.matcher(message);
        return m.find() ? m.group(1).trim() : null;
    }

    static String mergeWeighmentPayload(String existing, String orderId, String payloadJson) {
        String base = normalizeBase(existing, orderId);
        base = upsertSegment(base, "PAYLOAD", truncate(payloadJson, 3500));
        return upsertSegment(base, "FLAGS", joinFlags(addFlag(flags(base), FLAG_WEIGHMENT)));
    }

    static String mergeShopPhoto(String existing, String orderId, String fileKey) {
        String base = normalizeBase(existing, orderId);
        base = upsertSegment(base, "SHOP_KEY", sanitizeSegment(fileKey));
        return upsertSegment(base, "FLAGS", joinFlags(addFlag(flags(base), FLAG_SHOP_PHOTO)));
    }

    static String mergeCustomerAck(String existing, String orderId, String signatureKey, String customerPhotoKey) {
        String base = normalizeBase(existing, orderId);
        base = upsertSegment(base, "SIG_KEY", sanitizeSegment(signatureKey));
        base = upsertSegment(base, "CUST_PHOTO_KEY", sanitizeSegment(customerPhotoKey));
        return applyCustomerAckFlag(base);
    }

    static String mergeSignatureKey(String existing, String orderId, String signatureKey) {
        String base = normalizeBase(existing, orderId);
        base = upsertSegment(base, "SIG_KEY", sanitizeSegment(signatureKey));
        return applyCustomerAckFlag(base);
    }

    static String mergeCustomerPhotoKey(String existing, String orderId, String customerPhotoKey) {
        String base = normalizeBase(existing, orderId);
        base = upsertSegment(base, "CUST_PHOTO_KEY", sanitizeSegment(customerPhotoKey));
        return applyCustomerAckFlag(base);
    }

    private static String applyCustomerAckFlag(String message) {
        if (hasBothAckFiles(message)) {
            return upsertSegment(message, "FLAGS", joinFlags(addFlag(flags(message), FLAG_CUSTOMER_ACK)));
        }
        LinkedHashSet<String> without = new LinkedHashSet<>(flags(message));
        without.remove(FLAG_CUSTOMER_ACK);
        return upsertSegment(message, "FLAGS", joinFlags(without));
    }

    private static boolean hasBothAckFiles(String message) {
        return !segmentValue(message, "SIG_KEY").isBlank()
                && !segmentValue(message, "CUST_PHOTO_KEY").isBlank();
    }

    private static String segmentValue(String message, String key) {
        if (message == null) {
            return "";
        }
        Matcher m = SEGMENT_PATTERN.matcher(message);
        while (m.find()) {
            if (key.equals(m.group(1))) {
                String value = m.group(2);
                return value == null ? "" : value.trim();
            }
        }
        return "";
    }

    static String markUploadedComplete(String message) {
        if (message == null) {
            return PREFIX + "UPLOADED:Y";
        }
        return upsertSegment(message, "UPLOADED", "Y");
    }

    private static String normalizeBase(String existing, String orderId) {
        if (existing != null && isDeliveryWeighmentMessage(existing)) {
            String bound = boundOrderId(existing);
            if (bound != null && !bound.equals(orderId)) {
                throw new IllegalStateException("Order id mismatch in delivery message");
            }
            if (bound == null) {
                return existing + "|ORDER:" + sanitizeSegment(orderId);
            }
            return existing;
        }
        return PREFIX + "ORDER:" + sanitizeSegment(orderId);
    }

    private static LinkedHashSet<String> addFlag(Set<String> current, String flag) {
        LinkedHashSet<String> set = new LinkedHashSet<>(current);
        set.add(flag);
        return set;
    }

    private static String joinFlags(Set<String> flags) {
        return String.join(",", flags);
    }

    private static Set<String> flags(String message) {
        if (message == null) {
            return Set.of();
        }
        Matcher m = FLAGS_PATTERN.matcher(message);
        if (!m.find()) {
            return Set.of();
        }
        String raw = m.group(1);
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String part : raw.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                result.add(t);
            }
        }
        return result;
    }

    private static String upsertSegment(String message, String key, String value) {
        String needle = "|" + key + ":";
        String tokenValue = value == null ? "" : value;
        if (message == null) {
            return PREFIX + key + ":" + tokenValue;
        }
        int idx = message.indexOf(needle);
        if (idx < 0) {
            return message + "|" + key + ":" + tokenValue;
        }
        int valStart = idx + needle.length();
        int end = message.indexOf('|', valStart);
        if (end < 0) {
            return message.substring(0, valStart) + tokenValue;
        }
        return message.substring(0, valStart) + tokenValue + message.substring(end);
    }

    private static String sanitizeSegment(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "_").trim();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...";
    }
}
