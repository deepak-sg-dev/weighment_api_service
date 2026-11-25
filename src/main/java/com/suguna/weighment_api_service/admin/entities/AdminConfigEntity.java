package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "SUG_MAI_ADMIN_CFG")
public class AdminConfigEntity {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "APPLICATION", length = 32, nullable = false)
    private String application;

    @Column(name = "SOURCE", length = 32)
    private String source;

    @Column(name = "REGION_ID")
    private Long regionId;

    @Column(name = "BRANCH_ID")
    private Long branchId;

    @Column(name = "OPERATING_STYLE", length = 32)
    private String operatingStyle;

    @Column(name = "DEFAULT_UNIT", length = 16)
    private String defaultUnit;

    @Column(name = "ALLOW_LAME_BIRDS", length = 1)
    private String allowLameBirds;

    @Column(name = "ALLOW_MANUAL_WEIGHMENT", length = 1)
    private String allowManualWeighment;

    @Column(name = "LOCK_AVG_WEIGHT", length = 1)
    private String lockAvgWeight;

    @Column(name = "SCALE_INTEGRATION_ENABLED", length = 1)
    private String scaleIntegrationEnabled;

    @Column(name = "SCALE_CONNECTION_TYPE", length = 32)
    private String scaleConnectionType;

    @Column(name = "SCALE_PROTOCOL", length = 64)
    private String scaleProtocol;

    @Column(name = "AUTO_CAPTURE_STABLE_WEIGHT", length = 1)
    private String autoCaptureStableWeight;

    @Column(name = "MIN_WEIGHT_THRESHOLD", precision = 12, scale = 3)
    private BigDecimal minWeightThreshold;

    @Column(name = "WEIGHT_TOLERANCE_TYPE", length = 16)
    private String weightToleranceType;

    @Column(name = "WEIGHT_TOLERANCE_LIMIT", precision = 12, scale = 4)
    private BigDecimal weightToleranceLimit;

    @Column(name = "BIRD_COUNT_TOLERANCE")
    private Long birdCountTolerance;

    @Column(name = "CRATE_TARE_TOLERANCE", precision = 12, scale = 4)
    private BigDecimal crateTareTolerance;

    @Column(name = "ENFORCE_TOLERANCE_APPROVAL", length = 1)
    private String enforceToleranceApproval;

    @Column(name = "DC_SIGNING_REQUIRED", length = 1)
    private String dcSigningRequired;

    @Column(name = "OTP_ENABLED", length = 1)
    private String otpEnabled;

    @Column(name = "DC_PRINTING_STYLE", length = 64)
    private String dcPrintingStyle;

    @Column(name = "NXINFO_PASSWORD_HASH", length = 128)
    private String nxinfoPasswordHash;

    @Column(name = "SALES_HEAD_PWD_HASH", length = 128)
    private String salesHeadPwdHash;

    @Column(name = "GPS_MANDATORY", length = 1)
    private String gpsMandatory;

    @Column(name = "GPS_VARIANCE_LIMIT", precision = 10, scale = 2)
    private BigDecimal gpsVarianceLimit;

    @Column(name = "MANUAL_SCHEDULING", length = 1)
    private String manualScheduling;

    @Column(name = "SCHEDULE_LIMITING_HOURS")
    private Integer scheduleLimitingHours;

    @Column(name = "ACTIVE", length = 1, nullable = false)
    private String active;

    @Column(name = "REMARKS", length = 512)
    private String remarks;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;
}
