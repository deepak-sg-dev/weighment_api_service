package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchItemDTO {

    private Long id;
    private String branchCode;
    private String name;
    private String branchShortName;
    private String zoneName;
    private String region;
    private String status;
}