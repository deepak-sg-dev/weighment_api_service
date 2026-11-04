package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateScheduleVehicleResponse {

    private boolean success;
    private String message;
    private String scheduleId;
    private ScheduleVehicleDto vehicle;
    private String updatedAt;
}
