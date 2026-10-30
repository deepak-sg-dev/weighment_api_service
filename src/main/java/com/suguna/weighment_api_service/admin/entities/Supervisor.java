package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "SUG_MAI_DEVICEDATA")
@Getter
@Setter
public class Supervisor {

    @Id
    @Column(name = "EMP_CODE")
    private String empCode;

    @Column(name = "EMP_NAME")
    private String empName;

    @Column(name = "EMP_ID")
    private Long empId;

    @Column(name = "ORGANIZATION_ID")
    private Long organizationId;

    @Column(name = "DEVICE_ID")
    private Long deviceId;

    @Column(name = "DEVICE_INFO")
    private String deviceInfo;

    @Column(name = "CONTACT_NUMBER")
    private String contactNumber;

    @Column(name = "ANDROIDID")
    private String androidId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "APPLICATION")
    private String application;
}