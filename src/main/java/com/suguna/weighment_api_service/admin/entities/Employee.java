package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "SUG_HR_EMP_MST_MV")
@Getter
@Setter
public class Employee {

    @Id
    @Column(name = "PERSON_ID")
    private Long personId;

    @Column(name = "EMP_NO")
    private String empNo;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "NAME")
    private String name;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "JOB")
    private String job;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "MOBILE_NO")
    private String mobileNumber;

    @Column(name = "PHONE_NO")
    private String phoneNumber;

    @Column(name = "INVENTORY_ORGANIZATION_ID")
    private Long orgId;

}

