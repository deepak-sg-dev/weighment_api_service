package com.suguna.weighment_api_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps {@code SUG.SUG_MAI_DEVICEDATA}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Getter
@Setter
@NoArgsConstructor
@IdClass(SugMaiDevicedata.SugMaiDevicedataId.class)
@Entity
@Table(name = "SUG_MAI_DEVICEDATA", schema = "SUG")
public class SugMaiDevicedata {

    @Column(name = "ORGANIZATION_ID", nullable = false)
    private BigDecimal organizationId;

    @Id
    @Column(name = "DEVICE_ID")
    private BigDecimal deviceId;

    @Column(name = "ASSET_ID")
    private BigDecimal assetId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "INACTIVE_DATE")
    private LocalDateTime inactiveDate;

    @Column(name = "DEVICE_INFO")
    private String deviceInfo;

    @Column(name = "OS")
    private String os;

    @Id
    @Column(name = "EMP_CODE")
    private String empCode;

    @Column(name = "CONTACT_NUMBER")
    private String contactNumber;

    @Id
    @Column(name = "APPLICATION")
    private String application;

    @Column(name = "PWD")
    private String pwd;

    @Column(name = "DEVICE_ID_2")
    private BigDecimal deviceId2;

    @Column(name = "ENABLE_DB_EXPORT")
    private String enableDbExport;

    @Column(name = "EMP_NAME")
    private String empName;

    @Column(name = "EMP_ID")
    private BigDecimal empId;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "LAST_UPDATED_DATE")
    private LocalDateTime lastUpdatedDate;

    @Column(name = "CREATED_BY", nullable = false)
    private BigDecimal createdBy;

    @Column(name = "UPDATION_DATE")
    private LocalDateTime updationDate;

    @Column(name = "UPDATED_BY")
    private BigDecimal updatedBy;

    @Column(name = "USER_TYPE")
    private String userType;

    @Column(name = "UPLOADED_DATE")
    private LocalDateTime uploadedDate;

    @Column(name = "UPLOADED_MESSAGE")
    private String uploadedMessage;

    @Column(name = "UPLOADED_FLAG")
    private String uploadedFlag;

    @Column(name = "BRANCH_UPLOADED_FLAG")
    private String branchUploadedFlag;

    @Column(name = "ENABLE_GPS")
    private String enableGps;

    @Column(name = "LEDGER_ID", nullable = false)
    private BigDecimal ledgerId;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;

    @Column(name = "ANDROIDID")
    private String androidid;

    @Column(name = "FIREBASEID")
    private String firebaseid;

    @Column(name = "ORGANIZATION_ID_OLD")
    private BigDecimal organizationIdOld;

    @Column(name = "EMP_ID_OLD")
    private BigDecimal empIdOld;

    @Column(name = "LINE_ID")
    private BigDecimal lineId;

    @Column(name = "LAST_LOGIN_DATE")
    private LocalDateTime lastLoginDate;

    @Column(name = "RESET_PASS_DATE")
    private LocalDateTime resetPassDate;

    @Column(name = "LOGIN_TYPE")
    private String loginType;

    @Column(name = "LOGIN_ATTEMPT")
    private BigDecimal loginAttempt;

    @Column(name = "LOGIN_ATTEMPT_TIME")
    private String loginAttemptTime;

    @Column(name = "LOGIN_LOCK_STATUS")
    private BigDecimal loginLockStatus;

    @Column(name = "LOGIN_SOURCE")
    private String loginSource;

    @Column(name = "MPIN")
    private BigDecimal mpin;

    @Column(name = "PROFILE_PATH")
    private String profilePath;

    @Column(name = "LANGUAGE")
    private String language;

    @Column(name = "RESET_MPIN_DATE")
    private LocalDateTime resetMpinDate;

    @Getter
    @Setter
    public static class SugMaiDevicedataId implements java.io.Serializable {
        private BigDecimal deviceId;
        private String application;
        private String empCode;
    }
}