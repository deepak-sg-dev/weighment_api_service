package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

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

    private String companyShortName;

    private String companyImeiId2;

    private String companyImeiId3;

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
}
