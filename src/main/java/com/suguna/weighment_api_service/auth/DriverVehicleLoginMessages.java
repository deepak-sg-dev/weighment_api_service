package com.suguna.weighment_api_service.auth;

import com.suguna.weighment_api_service.constants.ErrorCodes;

/** User-facing copy for driver vehicle ↔ device login (Flutter shows {@code message}). */
public final class DriverVehicleLoginMessages {

    public static final String INVALID_VEHICLE_NO =
            "Enter a valid vehicle number (at least 6 characters, letters and digits).";

    public static final String VEHICLE_NOT_REGISTERED =
            "This vehicle number is not registered. Kindly register the vehicle in the master and try again.";

    public static final String VEHICLE_DEVICE_MISMATCH =
            "This vehicle is not registered on this device. Please register it and try again.";

    public static final String VEHICLE_NOT_DRIVER =
            "This vehicle is not assigned for DRIVER login. Kindly contact your administrator.";

    public static final String VEHICLE_MAPPING_EXPIRED =
            "This vehicle mapping has expired. Kindly register the vehicle for this device again.";

    public static final String VEHICLE_MASTER_UNAVAILABLE =
            "Unable to verify the vehicle just now. Please try again in a few minutes.";

    private DriverVehicleLoginMessages() {}

    public static String forErrorCode(String errorCode) {
        if (errorCode == null) {
            return VEHICLE_DEVICE_MISMATCH;
        }
        return switch (errorCode) {
            case ErrorCodes.VEHICLE_NOT_REGISTERED -> VEHICLE_NOT_REGISTERED;
            case ErrorCodes.VEHICLE_DEVICE_MISMATCH -> VEHICLE_DEVICE_MISMATCH;
            case ErrorCodes.VEHICLE_NOT_DRIVER -> VEHICLE_NOT_DRIVER;
            case ErrorCodes.VEHICLE_MAPPING_EXPIRED -> VEHICLE_MAPPING_EXPIRED;
            default -> VEHICLE_DEVICE_MISMATCH;
        };
    }
}
