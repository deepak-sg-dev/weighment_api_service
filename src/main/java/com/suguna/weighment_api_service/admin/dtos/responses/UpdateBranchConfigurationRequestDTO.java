package com.suguna.weighment_api_service.admin.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBranchConfigurationRequestDTO {

    private Boolean manualScheduleAllowed;

    private Boolean lameBirdsEnabled;

    private String operatingStyle;

    private String defaultUnit;

    private Boolean scheduleWeightLimitEnabled;

    private Integer limitingHours;

    private Integer normalBirdExcessAllowedPercent;

    private Integer weakBirdExcessAllowedPercent;
}