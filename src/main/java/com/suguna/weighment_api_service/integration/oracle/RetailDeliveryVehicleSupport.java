package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleVehicleDto;

/** Resolves per-order delivery vehicle from DTL columns, with legacy {@code message} token fallback. */
public final class RetailDeliveryVehicleSupport {

    private RetailDeliveryVehicleSupport() {}

    public static ScheduleVehicleDto toDto(
            String delvVehicleNo, String delvDriver, String delvDriverMobile, String message) {
        String number = coalesce(nullIfBlank(delvVehicleNo), RetailDeliveryVehicleMessageSupport.readVehicleNo(message));
        String driverName =
                coalesce(nullIfBlank(delvDriver), RetailDeliveryVehicleMessageSupport.readDriverName(message));
        String driverMobile = coalesce(
                nullIfBlank(delvDriverMobile), RetailDeliveryVehicleMessageSupport.readDriverMobile(message));
        if (number == null && driverName == null && driverMobile == null) {
            return null;
        }
        ScheduleVehicleDto dto = new ScheduleVehicleDto();
        dto.setNumber(number);
        dto.setDriverName(driverName);
        dto.setDriverMobile(driverMobile);
        return dto;
    }

    private static String coalesce(String primary, String fallback) {
        return primary != null ? primary : fallback;
    }

    private static String nullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
