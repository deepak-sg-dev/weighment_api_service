package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "SUG_MAI_USER", schema = "SUG")
@Getter
@Setter
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "SUG_MAI_USER_S", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_ID")
    private Long userId; // business user id

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "PASS_WORD")
    private String password;

    @Column(name = "NAME")
    private String name;

    @Column(name = "MOBILE")
    private Long mobile;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "ORG_ID")
    private Long orgId;

    @Column(name = "USER_TYPE")
    private String userType; // CUSTOMER / EMPLOYEE

    @Column(name = "APPLICATION")
    private String application;

    @Column(name = "STATUS")
    private String status; // A, I, B

    @Column(name = "END_DATE")
    private Date endDate;

    @Column(name = "LAST_LOGIN_AT")
    private Date lastLoginAt;

    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    private Date updatedAt;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    @Column(name = "PASSWORD_CHANGED_AT")
    private Date passwordChangedAt;

    @Column(name = "PASSWORD_EXPIRY_DAYS")
    private Integer passwordExpiryDays;

    @Column(name = "FIREBASE_TOKEN")
    private String firebaseToken;  // ✅ FCM Device


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID", insertable = false, updatable = false)
    private Role role;


    @Column(name = "ROLE_ID")
    private Long roleId;
}
