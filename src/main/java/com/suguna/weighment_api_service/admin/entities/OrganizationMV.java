package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "SUG_ORGANIZATION_MV", schema = "APPS")
@Getter
@Setter
public class OrganizationMV {

    @Id
    @Column(name = "BRANCH_ID")
    private Long branchId;

    @Column(name = "BRANCH_CODE")
    private String branchCode;

    @Column(name = "BRANCH_NAME")
    private String branchName;
}