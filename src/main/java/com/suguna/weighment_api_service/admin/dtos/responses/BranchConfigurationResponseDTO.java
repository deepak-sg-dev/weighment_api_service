package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BranchConfigurationResponseDTO {

    private Long branchId;

    private String branchName;

    private String address;

    private String managerMobile;

    private Boolean manualScheduleAllowed;

    private Boolean lameBirdsEnabled;

    private String operatingStyle;

    private List<String> deviceIdentifiers;

    private String defaultUnit;

    private String userCategory;

    private String gstin;

    private String gstinDate;

    private Boolean scheduleWeightLimitEnabled;

    private Integer limitingHours;

    private Integer normalBirdExcessAllowedPercent;

    private Integer weakBirdExcessAllowedPercent;

    private String bankName;

    private String bankBranch;

    private String bankAccountMasked;

    private String bankIfsc;
}