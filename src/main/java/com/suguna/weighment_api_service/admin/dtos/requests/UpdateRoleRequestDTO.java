package com.suguna.weighment_api_service.admin.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequestDTO {

    private String roleName;

    private String roleDescription;

    private String roleFor;

    private String status;
}