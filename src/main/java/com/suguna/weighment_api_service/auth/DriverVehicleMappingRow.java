package com.suguna.weighment_api_service.auth;

import java.time.LocalDate;

/** One {@code sug_mai_lifting_vehicle_mst} row used for driver login checks. */
public record DriverVehicleMappingRow(
        String transId,
        String vehicleNo,
        String vehicleType,
        String deviceId,
        String androidId,
        String deviceId2,
        String userType,
        String mobileNo,
        LocalDate fromDate,
        LocalDate toDate) {}
