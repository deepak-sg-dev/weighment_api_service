package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchDetailResponseDTO {

    private Long id;
    private String branchCode;
    private String branchName;
    private String branchShortName;

    private String zoneName;
    private String region;
    private String stateName;
    private String locationName;

    private String processEnabledFlag;
    private String status;
}