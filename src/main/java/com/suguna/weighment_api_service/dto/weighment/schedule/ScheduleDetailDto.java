package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleDetailDto {

    private String scheduleId;
    private FarmDto farm;
    private PartyContactDto farmer;
    private PartyContactDto trader;
    private ProductDto product;
    private int plannedQuantity;
    private String operationType;
    private String scaleType;
    private String status;
    private boolean downloaded;
    private ScheduleRulesDto rules;
}
