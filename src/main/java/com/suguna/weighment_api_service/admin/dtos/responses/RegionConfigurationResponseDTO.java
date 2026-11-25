package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RegionConfigurationResponseDTO {

    private Long regionId;
    private String regionCode;
    private String regionName;
    private String zoneName;
    private Long branchCount;

    private String operatingStyle;
    private String defaultUnit;
    private Boolean allowLameBirds;
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
    private Boolean manualScheduling;
    private Integer scheduleLimitingHours;
    private String remarks;
}
