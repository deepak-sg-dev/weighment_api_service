package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermissionDTO {

    private Long menuId;

    private String menuName;

    private Boolean canRead;

    private Boolean canWrite;

    private Boolean canUpdate;

    private Boolean canDelete;
}