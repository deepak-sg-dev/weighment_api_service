package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "SUG_ORGANIZATION_MV")
public class OrganizationBranch {

    @Id
    @Column(name = "BRANCH_ID")
    private Long branchId;

    @Column(name = "BRANCH_CODE")
    private String branchCode;

    @Column(name = "BRANCH_NAME")
    private String branchName;

    @Column(name = "BRANCH_SHORT_NAME")
    private String branchShortName;

    @Column(name = "ZONE_NAME")
    private String zoneName;

    @Column(name = "REGION")
    private String region;

    @Column(name = "STATE_NAME")
    private String stateName;

    @Column(name = "LOCATION_NAME")
    private String locationName;

    @Column(name = "DISABLE_DATE")
    private Date disableDate;

    @Column(name = "PROCESS_ENABLED_FLAG")
    private String processEnabledFlag;

    @Column(name = "DIVISION_NAME")
    private String divisionName;
}