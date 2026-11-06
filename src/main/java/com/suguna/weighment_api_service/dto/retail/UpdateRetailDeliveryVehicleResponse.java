package com.suguna.weighment_api_service.dto.retail;

import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleVehicleDto;
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
public class UpdateRetailDeliveryVehicleResponse {

    private boolean success;
    private String message;
    private String scheduleId;
    private String retailOrderId;
    private ScheduleVehicleDto deliveryVehicle;
    private String updatedAt;
}
