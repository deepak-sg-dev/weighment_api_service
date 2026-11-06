package com.suguna.weighment_api_service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WeighmentEntryCategoryTest {

    @Test
    void emptyIsNotBirdAdjustment() {
        assertFalse(WeighmentEntryCategory.requiresBirdAdjustment(WeighmentEntryCategory.EMPTY));
    }

    @Test
    void lameAndReplacementAreAdjustments() {
        assertTrue(WeighmentEntryCategory.requiresBirdAdjustment(WeighmentEntryCategory.LAME));
        assertTrue(WeighmentEntryCategory.requiresBirdAdjustment(WeighmentEntryCategory.REPLACEMENT));
    }

    @Test
    void normalizeEmptyCategory() {
        assertEquals("EMPTY", WeighmentEntryCategory.normalize("empty"));
    }
}
