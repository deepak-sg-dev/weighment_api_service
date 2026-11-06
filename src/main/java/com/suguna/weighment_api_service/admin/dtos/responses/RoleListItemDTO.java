package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleListItemDTO {

    private Long id;

    private String roleName;

    private String roleDescription;

    private String roleFor;

    private String status;
}