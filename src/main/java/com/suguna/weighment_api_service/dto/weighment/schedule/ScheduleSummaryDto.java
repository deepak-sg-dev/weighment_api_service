package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleSummaryDto {

    private String scheduleId;
    private String farmId;
    private String farmName;
    private String operationType;
    private String scaleType;
    private int plannedQuantity;
    private String status;
    private boolean downloaded;
}
