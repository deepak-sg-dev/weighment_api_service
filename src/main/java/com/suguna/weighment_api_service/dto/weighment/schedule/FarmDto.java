package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FarmDto {

    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private int geofenceRadiusMeters;
}
