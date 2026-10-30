package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyConfigurationResponseDTO {

    private String zoneName;
    private String companyShortName;

    private String address;
    private String panNo;
    private String zoneMobile;
    private String zoneEmail;

    private String website;
    private String customerCareNo;

    private String cinNumber;
    private String gstin;

    private String bankName;
    private String bankBranch;
    private String bankIfsc;

    private Boolean otpEnabled;
    private Boolean gpsMandatory;
    private Boolean dcSigningEnabled;
}