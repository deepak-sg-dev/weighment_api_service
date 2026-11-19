package com.suguna.weighment_api_service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.suguna.weighment_api_service.constants.ErrorCodes;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DriverVehicleMappingEvaluatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);

    @Test
    void noRows_vehicleNotRegistered() {
        assertEquals(
                ErrorCodes.VEHICLE_NOT_REGISTERED,
                DriverVehicleMappingEvaluator.evaluate("DEV1", List.of(), TODAY));
    }

    @Test
    void vehicleOnOtherDevice_mismatchMessageCode() {
        DriverVehicleMappingRow other = row("KA01AB1234", "OTHER_DEVICE", "DRIVER", null);
        assertEquals(
                ErrorCodes.VEHICLE_DEVICE_MISMATCH,
                DriverVehicleMappingEvaluator.evaluate("DEV1", List.of(other), TODAY));
        assertEquals(
                "This vehicle is not registered on this device. Please register it and try again.",
                DriverVehicleLoginMessages.forErrorCode(ErrorCodes.VEHICLE_DEVICE_MISMATCH));
    }

    @Test
    void mappedButNotDriver() {
        DriverVehicleMappingRow emp = row("KA01AB1234", "DEV1", "EMPLOYEE", null);
        assertEquals(
                ErrorCodes.VEHICLE_NOT_DRIVER,
                DriverVehicleMappingEvaluator.evaluate("DEV1", List.of(emp), TODAY));
    }

    @Test
    void driverExpired() {
        DriverVehicleMappingRow expired = row("KA01AB1234", "DEV1", "DRIVER", TODAY.minusDays(1));
        assertEquals(
                ErrorCodes.VEHICLE_MAPPING_EXPIRED,
                DriverVehicleMappingEvaluator.evaluate("DEV1", List.of(expired), TODAY));
    }

    @Test
    void driverActiveToDateToday_ok() {
        DriverVehicleMappingRow active = row("KA01AB1234", "DEV1", "DRIVER", TODAY);
        assertNull(DriverVehicleMappingEvaluator.evaluate("DEV1", List.of(active), TODAY));
    }

    @Test
    void driverOpenEndedToDate_ok() {
        DriverVehicleMappingRow open = row("KA01AB1234", "DEV1", "driver", null);
        assertNull(DriverVehicleMappingEvaluator.evaluate("DEV1", List.of(open), TODAY));
    }

    private static DriverVehicleMappingRow row(String vehicle, String device, String type, LocalDate toDate) {
        return new DriverVehicleMappingRow(null, vehicle, null, device, null, null, type, null, null, toDate);
    }
}
