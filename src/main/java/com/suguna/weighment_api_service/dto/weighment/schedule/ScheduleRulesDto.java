package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleRulesDto {

    private int standardBirdsPerCage;
    private long stableDurationMs;
    private double toleranceMin;
    private double toleranceMax;
    private String captureMode;
    private PhotoRequirementDto photoRequirement;
}
