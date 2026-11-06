package com.suguna.weighment_api_service.admin.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleMenuDTO {

    private Long menuId;

    private Boolean canRead;

    private Boolean canWrite;

    private Boolean canUpdate;

    private Boolean canDelete;
}