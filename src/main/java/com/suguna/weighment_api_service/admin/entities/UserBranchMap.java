package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "SUG_MAI_USER_BRANCH_MAP", schema = "SUG")
@Getter
@Setter
public class UserBranchMap {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "BRANCH_ID")
    private Long branchId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_AT")
    private Date updatedAt;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;
}