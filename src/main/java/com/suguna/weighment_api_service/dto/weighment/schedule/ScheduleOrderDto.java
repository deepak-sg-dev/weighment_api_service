package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleOrderDto {

    private String customerCode;
    private String customerName;
    private double orderedQuantityKg;
    private String location;
    private Double latitude;
    private Double longitude;
}
