package com.suguna.weighment_api_service.auth;

import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.util.VehicleRegistrationSupport;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Decides why driver login failed so the API can return a specific, user-friendly message.
 */
public final class DriverVehicleMappingEvaluator {

    private DriverVehicleMappingEvaluator() {}

    public static String evaluate(String requestDeviceId, List<DriverVehicleMappingRow> rows, LocalDate today) {
        if (rows == null || rows.isEmpty()) {
            return ErrorCodes.VEHICLE_NOT_REGISTERED;
        }
        String device = compactDevice(requestDeviceId);
        List<DriverVehicleMappingRow> onThisDevice = rows.stream()
                .filter(row -> deviceMatches(device, row))
                .toList();
        if (onThisDevice.isEmpty()) {
            return ErrorCodes.VEHICLE_DEVICE_MISMATCH;
        }
        List<DriverVehicleMappingRow> driverRows = onThisDevice.stream()
                .filter(DriverVehicleMappingEvaluator::isDriver)
                .toList();
        if (driverRows.isEmpty()) {
            return ErrorCodes.VEHICLE_NOT_DRIVER;
        }
        boolean anyActive = driverRows.stream().anyMatch(row -> isActive(row, today));
        if (!anyActive) {
            return ErrorCodes.VEHICLE_MAPPING_EXPIRED;
        }
        return null;
    }

    public static DriverVehicleMappingRow firstActiveDriver(
            String requestDeviceId, List<DriverVehicleMappingRow> rows, LocalDate today) {
        if (evaluate(requestDeviceId, rows, today) != null) {
            return null;
        }
        String device = compactDevice(requestDeviceId);
        return rows.stream()
                .filter(row -> deviceMatches(device, row))
                .filter(DriverVehicleMappingEvaluator::isDriver)
                .filter(row -> isActive(row, today))
                .findFirst()
                .orElse(null);
    }

    static boolean deviceMatches(String compactRequestDevice, DriverVehicleMappingRow row) {
        if (compactRequestDevice == null || compactRequestDevice.isEmpty()) {
            return false;
        }
        return compactRequestDevice.equalsIgnoreCase(compactDevice(row.deviceId()))
                || compactRequestDevice.equalsIgnoreCase(compactDevice(row.androidId()))
                || compactRequestDevice.equalsIgnoreCase(compactDevice(row.deviceId2()));
    }

    static boolean isDriver(DriverVehicleMappingRow row) {
        String type = row.userType() == null ? "" : row.userType().trim().toUpperCase(Locale.ROOT);
        return "DRIVER".equals(type);
    }

    static boolean isActive(DriverVehicleMappingRow row, LocalDate today) {
        LocalDate day = today == null ? LocalDate.now() : today;
        if (row.fromDate() != null && row.fromDate().isAfter(day)) {
            return false;
        }
        return row.toDate() == null || !row.toDate().isBefore(day);
    }

    static String compactDevice(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }

    public static String requireCompactVehicleNo(String raw) {
        String compact = VehicleRegistrationSupport.normalizeOrNull(raw);
        if (compact == null) {
            return null;
        }
        return compact;
    }
}
