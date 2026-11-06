package com.suguna.weighment_api_service.admin.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateRoleRequestDTO {

    private String roleName;

    private String roleDescription;

    private String roleFor;

    private List<RoleMenuDTO> menus;
}
