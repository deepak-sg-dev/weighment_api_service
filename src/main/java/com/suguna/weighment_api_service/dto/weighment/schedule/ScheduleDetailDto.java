package com.suguna.weighment_api_service.dto.weighment.schedule;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleDetailDto {

    private String scheduleId;
    private String status;
    private boolean downloaded;
    private String saleType;
    private String birdType;
    private String scheduledAt;
    private double plannedQuantity;
    private String scaleType;
    private String liftingStatus;
    private String deliveryStatus;
    private String liftStage;
    private ScheduleBranchDto branch;
    private String salesOrderNo;
    private Double declaredBodyWeightKg;
    private ScheduleVehicleDto vehicle;
    private ScheduleSupervisorDto supervisor;
    private FarmDto farm;
    private PartyContactDto farmer;
    private ScheduleRulesDto rules;
    private List<ScheduleDeliveryDto> deliveries;
}
