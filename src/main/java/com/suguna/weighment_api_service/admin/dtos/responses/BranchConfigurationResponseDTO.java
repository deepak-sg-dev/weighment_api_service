package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class BranchConfigurationResponseDTO {

    private Long branchId;
    private String branchCode;
    private String branchName;
    private String branchShortName;
    private Long regionId;
    private String regionName;
    private String zoneName;
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

    private Boolean allowManualWeighment;
    private Boolean lockAvgWeight;
    private Boolean scaleIntegrationEnabled;
    private String scaleConnectionType;
    private String scaleProtocol;
    private Boolean autoCaptureStableWeight;
    private BigDecimal minWeightThreshold;
    private String weightToleranceType;
    private BigDecimal weightToleranceLimit;
    private Long birdCountTolerance;
    private BigDecimal crateTareTolerance;
    private Boolean enforceToleranceApproval;
    private Boolean dcSigningRequired;
    private Boolean otpEnabled;
    private String dcPrintingStyle;
    private Boolean gpsMandatory;
    private BigDecimal gpsVarianceLimit;
    private String remarks;
}
