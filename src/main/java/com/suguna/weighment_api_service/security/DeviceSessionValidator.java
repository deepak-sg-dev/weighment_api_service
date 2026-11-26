package com.suguna.weighment_api_service.security;

import com.suguna.weighment_api_service.auth.DriverVehicleMappingEvaluator;
import com.suguna.weighment_api_service.auth.DriverVehicleMappingRow;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.integration.oracle.DriverVehicleMappingSqlClient;
import com.suguna.weighment_api_service.repository.DeviceRegistry;
import com.suguna.weighment_api_service.util.VehicleRegistrationSupport;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Employee login may use ANDROIDID / DEVICE_ID / DEVICE_ID_2. Driver login is bound to
 * {@code SUG_MAI_LIFTING_VEHICLE_MST.DEVICE_ID}; mobile submit often sends a different alias
 * (Android ID vs scale id). A DRIVER token whose JWT device is on that vehicle is allowed to
 * submit; {@code deviceId} is still stored from the request body.
 */
@Component
public class DeviceSessionValidator {

    private final DeviceRegistry deviceRegistry;
    private final DriverVehicleMappingSqlClient driverVehicleMappingSqlClient;

    public DeviceSessionValidator(
            DeviceRegistry deviceRegistry, DriverVehicleMappingSqlClient driverVehicleMappingSqlClient) {
        this.deviceRegistry = deviceRegistry;
        this.driverVehicleMappingSqlClient = driverVehicleMappingSqlClient;
    }

    public void requireMatches(AuthenticatedSupervisor supervisor, String requestDeviceId) {
        if (matches(supervisor.getDeviceId(), requestDeviceId)) {
            return;
        }
        if (supervisor.isDriver()) {
            if (requestDeviceId != null
                    && !requestDeviceId.isBlank()
                    && jwtDeviceMappedToVehicle(supervisor)) {
                return;
            }
        } else if (requestDeviceId != null && !requestDeviceId.isBlank()) {
            var registration = deviceRegistry.findByDeviceId(requestDeviceId.trim());
            if (registration.isPresent() && matches(supervisor.getDeviceId(), registration.get().getDeviceId())) {
                return;
            }
        }
        throw new ApiException(ErrorCode.BAD_REQUEST, "deviceId does not match authenticated session");
    }

    private boolean jwtDeviceMappedToVehicle(AuthenticatedSupervisor session) {
        String plate = VehicleRegistrationSupport.normalizeOrNull(session.getVehicleNo());
        if (plate == null) {
            return false;
        }
        String jwtDevice = compact(session.getDeviceId());
        if (jwtDevice.isEmpty()) {
            return false;
        }
        List<DriverVehicleMappingRow> rows = driverVehicleMappingSqlClient.findByCompactVehicleNo(plate);
        for (DriverVehicleMappingRow row : rows) {
            if (DriverVehicleMappingEvaluator.isDriver(row)
                    && DriverVehicleMappingEvaluator.deviceMatches(jwtDevice, row)) {
                return true;
            }
        }
        return false;
    }

    static boolean matches(String sessionDeviceId, String requestDeviceId) {
        String a = compact(sessionDeviceId);
        String b = compact(requestDeviceId);
        return !a.isEmpty() && a.equals(b);
    }

    static String compact(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }
}
