package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleDeliveryDto {

    private String deliveryId;
    private int sequence;
    private String dropPointName;
    private String customerCode;
    private String customerName;
    private String location;
    private Double latitude;
    private Double longitude;
    private int geofenceRadiusMeters;
    private double quantityKg;
    private String status;
}
