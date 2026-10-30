package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@IdClass(DeviceId.class)
@Table(name = "SUG_MAI_DEVICEDATA", schema = "SUG")
public class Device {

    @Id
    @Column(name = "DEVICE_ID")
    private BigDecimal deviceId;

    @Id
    @Column(name = "EMP_CODE")
    private String empCode;

    @Id
    @Column(name = "APPLICATION")
    private String application;

    @Column(name = "ORGANIZATION_ID")
    private BigDecimal organizationId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "INACTIVE_DATE")
    private String inactiveDate;

    @Column(name = "DEVICE_INFO")
    private String deviceInfo;

    @Column(name = "OS")
    private String os;

    @Column(name = "CONTACT_NUMBER")
    private String contactNumber;

    @Column(name = "PWD")
    private String pwd;

    @Column(name = "DEVICE_ID_2")
    private String deviceId2;

    @Column(name = "ENABLE_DB_EXPORT")
    private String enableDbExport;

    @Column(name = "EMP_NAME")
    private String empName;

    @Column(name = "EMP_ID")
    private BigDecimal empId;

    @Column(name = "CREATION_DATE")
    private java.util.Date creationDate;

    @Column(name = "LAST_UPDATED_DATE")
    private java.util.Date lastUpdatedDate;

    @Column(name = "CREATED_BY")
    private BigDecimal createdBy;

    @Column(name = "UPDATION_DATE")
    private java.util.Date updationDate;

    @Column(name = "UPDATED_BY")
    private BigDecimal updatedBy;

    @Column(name = "USER_TYPE")
    private String userType;

    @Column(name = "ANDROIDID")
    private String androidId;

    @Column(name = "FIREBASEID")
    private String firebaseId;

    @Column(name = "LEDGER_ID")
    private BigDecimal ledgerId;
}