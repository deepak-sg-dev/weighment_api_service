package com.suguna.weighment_api_service.util;

import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** V2: five live scale readings per weighment entry. */
public final class LiveReadingValidation {

    private static final int REQUIRED_COUNT = 5;
    private static final double AVERAGE_TOLERANCE = 0.01;

    private LiveReadingValidation() {}

    public static void validateEntry(Integer sequence, List<Double> liveReadings, Double liveReadingsAverage) {
        if (liveReadings == null && liveReadingsAverage == null) {
            return;
        }
        if (liveReadings == null || liveReadingsAverage == null) {
            throw entryError(sequence, "liveReadings and liveReadingsAverage are required together");
        }
        if (liveReadings.size() != REQUIRED_COUNT) {
            throw entryError(sequence, "liveReadings must contain exactly " + REQUIRED_COUNT + " values");
        }
        double sum = 0D;
        for (Double reading : liveReadings) {
            if (reading == null || reading <= 0D) {
                throw entryError(sequence, "each live reading must be greater than 0");
            }
            if (decimalPlaces(reading) > 2) {
                throw entryError(sequence, "live readings allow at most 2 decimal places");
            }
            sum += reading;
        }
        double expected = BigDecimal.valueOf(sum / REQUIRED_COUNT)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        if (Math.abs(expected - liveReadingsAverage) > AVERAGE_TOLERANCE) {
            throw entryError(sequence, "liveReadingsAverage does not match the average of liveReadings");
        }
    }

    private static int decimalPlaces(double value) {
        String text = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
        int dot = text.indexOf('.');
        return dot < 0 ? 0 : text.length() - dot - 1;
    }

    private static BusinessValidationException entryError(Integer sequence, String detail) {
        String seq = sequence != null ? String.valueOf(sequence) : "?";
        return new BusinessValidationException(
                ErrorCodes.VALIDATION_FAILED, "Weighment entry sequence " + seq + ": " + detail);
    }
}
