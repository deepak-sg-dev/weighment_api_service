package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CompanyConfigurationResponseDTO {

    private String companyName;
    private String panNo;
    private String address;
    private String password;
    private String companyMobile;
    private String companyEmail;
    private String companyImeiId1;
    private String companyImeiId2;
    private String companyImeiId3;
    private String companyShortName;
    private String cinNumber;
    private String website;
    private String customerCareNo;
    private String gstin;
    private String hsnCode;
    private Integer taxRate;
    private String bankName;
    private String bankBranch;
    private String bankAccountNo;
    private String bankIfsc;
    private String effectiveDate;
    private String autoSiteCode;

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
