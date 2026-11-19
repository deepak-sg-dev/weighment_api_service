package com.suguna.weighment_api_service.admin.dtos.requests;

import lombok.Data;

@Data
public class UpdateUserRequestDTO {

    private String name;
    private String mobile;
    private String email;
    private Long roleId;
    private Long branchId;
    private String userType;
    private Boolean active;
    private String password;
}