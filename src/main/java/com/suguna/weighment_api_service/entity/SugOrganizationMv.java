package com.suguna.weighment_api_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Immutable;

/**
 * Maps {@code APPS.SUG_ORGANIZATION_MV}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Immutable
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SUG_ORGANIZATION_MV", schema = "APPS")
public class SugOrganizationMv {

    @Column(name = "ZONE_NAME")
    private String zoneName;

    @Column(name = "REGION_ID")
    private BigDecimal regionId;

    @Column(name = "REGION_CODE")
    private String regionCode;

    @Column(name = "REGION")
    private String region;

    @Id
    @Column(name = "BRANCH_ID")
    private BigDecimal branchId;

    @Column(name = "BRANCH_CODE")
    private String branchCode;

    @Column(name = "BRANCH_NAME")
    private String branchName;

    @Column(name = "BRANCH_SHORT_NAME")
    private String branchShortName;

    @Column(name = "AREA")
    private String area;

    @Column(name = "GL_REGION")
    private String glRegion;

    @Column(name = "LEDGER_ID")
    private BigDecimal ledgerId;

    @Column(name = "CHART_OF_ACCOUNTS_ID")
    private BigDecimal chartOfAccountsId;

    @Column(name = "GL_DIVISION")
    private String glDivision;

    @Column(name = "GL_LOCATION")
    private String glLocation;

    @Column(name = "DIVISION_NAME")
    private String divisionName;

    @Column(name = "LOCATION_NAME")
    private String locationName;

    @Column(name = "OPM_DIVISION")
    private String opmDivision;

    @Column(name = "BUSINESS_GROUP_ID")
    private BigDecimal businessGroupId;

    @Column(name = "DISABLE_DATE")
    private LocalDateTime disableDate;

    @Column(name = "PARENT_BRANCH_ID")
    private BigDecimal parentBranchId;

    @Column(name = "PARENT_BRANCH_CODE")
    private String parentBranchCode;

    @Column(name = "PARENT_BRANCH_NAME")
    private String parentBranchName;

    @Column(name = "BREEDER_BRANCH")
    private String breederBranch;

    @Column(name = "PROCESS_ENABLED_FLAG")
    private String processEnabledFlag;

    @Column(name = "MASTER_ORGANIZATION_ID")
    private BigDecimal masterOrganizationId;

    @Column(name = "MASTER_ORGANIZATION_CODE")
    private String masterOrganizationCode;

    @Column(name = "OPM_ZONE")
    private String opmZone;

    @Column(name = "ZONE_SEQ")
    private String zoneSeq;

    @Column(name = "REGION_SEQ")
    private String regionSeq;

    @Column(name = "MATERIAL_ACCOUNT_ID")
    private BigDecimal materialAccountId;

    @Column(name = "HR_LOCATION_ID")
    private BigDecimal hrLocationId;

    @Column(name = "ORG_TYPE")
    private String orgType;

    @Column(name = "STATE_NAME")
    private String stateName;

    @Column(name = "ORGN_SHORT_NAME")
    private String orgnShortName;

    @Column(name = "CLUSTER_NAME")
    private String clusterName;

    @Column(name = "CLUSTER_DESC")
    private String clusterDesc;

    @Column(name = "CLUSTER_NAME_SEQ")
    private BigDecimal clusterNameSeq;

    @Column(name = "CURRENCY_CODE")
    private String currencyCode;

}