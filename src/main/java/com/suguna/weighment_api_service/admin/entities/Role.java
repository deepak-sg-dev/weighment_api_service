package com.suguna.weighment_api_service.admin.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "SUG_MAI_ROLE", schema = "SUG")
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
    @SequenceGenerator(name = "role_seq", sequenceName = "SUG_MAI_ROLE_S", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "APPLICATION")
    private String application;

    @Column(name = "ROLE_NAME")
    private String roleName;

    @Column(name = "ROLE_DESC")
    private String roleDesc;

    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    private Date updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "FEEDMILL_REQUIRED")
    private String feedmillRequired;

    @Column(name = "FRANCHISEE_REQUIRED")
    private String franchiseeRequired;

    @Column(name = "ROLE_FOR")
    private String roleFor;

}
