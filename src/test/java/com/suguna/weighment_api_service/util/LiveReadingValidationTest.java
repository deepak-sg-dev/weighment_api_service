package com.suguna.weighment_api_service.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.suguna.weighment_api_service.exception.BusinessValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

class LiveReadingValidationTest {

    @Test
    void acceptsValidFiveReadings() {
        assertDoesNotThrow(() -> LiveReadingValidation.validateEntry(
                1, List.of(24.52, 24.58, 24.55, 24.57, 24.58), 24.56));
    }

    @Test
    void rejectsWrongCount() {
        assertThrows(
                BusinessValidationException.class,
                () -> LiveReadingValidation.validateEntry(2, List.of(1.0, 2.0), 1.5));
    }
}
