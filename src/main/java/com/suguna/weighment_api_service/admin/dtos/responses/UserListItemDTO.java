package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserListItemDTO {

    private Long id;

    private Long userId;

    private String username;

    private String name;

    private String mobile;

    private String email;

    private Long orgId;

    private String branchId;

    private String userType;

    private String application;

    private Long roleId;

    private String roleName;

    private String status;

    private Date lastLoginAt;

    private Date createdAt;
}
