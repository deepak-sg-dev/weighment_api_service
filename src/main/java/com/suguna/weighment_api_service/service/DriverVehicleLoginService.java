package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.auth.DriverVehicleLoginMessages;
import com.suguna.weighment_api_service.auth.DriverVehicleMappingEvaluator;
import com.suguna.weighment_api_service.auth.DriverVehicleMappingRow;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import com.suguna.weighment_api_service.integration.oracle.DriverVehicleMappingSqlClient;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class DriverVehicleLoginService {

    private static final Logger log = LoggerFactory.getLogger(DriverVehicleLoginService.class);

    private final DriverVehicleMappingSqlClient mappingSqlClient;

    public DriverVehicleLoginService(DriverVehicleMappingSqlClient mappingSqlClient) {
        this.mappingSqlClient = mappingSqlClient;
    }

    /**
     * When login sends {@code vehicleNo}, the plate must be an active DRIVER mapping for this
     * {@code deviceId}. Throws 422 with a message the app can show as-is.
     *
     * @return the active DRIVER mapping row for this device
     */
    public DriverVehicleMappingRow requireVehicleRegisteredToDevice(String deviceId, String vehicleNo) {
        String compact = DriverVehicleMappingEvaluator.requireCompactVehicleNo(vehicleNo);
        if (compact == null) {
            throw new BusinessValidationException(
                    ErrorCodes.VALIDATION_FAILED, DriverVehicleLoginMessages.INVALID_VEHICLE_NO);
        }
        List<DriverVehicleMappingRow> rows;
        try {
            rows = mappingSqlClient.findByCompactVehicleNo(compact);
        } catch (DataAccessException ex) {
            log.warn("Vehicle master lookup failed for vehicleNo={}: {}", compact, ex.getMessage());
            throw new ApiException(
                    ErrorCode.ERP_UNAVAILABLE, DriverVehicleLoginMessages.VEHICLE_MASTER_UNAVAILABLE, ex);
        }
        LocalDate today = LocalDate.now();
        String errorCode = DriverVehicleMappingEvaluator.evaluate(deviceId, rows, today);
        if (errorCode != null) {
            throw new BusinessValidationException(
                    errorCode,
                    DriverVehicleLoginMessages.forErrorCode(errorCode),
                    Map.of("vehicleNo", compact, "deviceId", deviceId == null ? "" : deviceId.trim()));
        }
        return DriverVehicleMappingEvaluator.firstActiveDriver(deviceId, rows, today);
    }
}
