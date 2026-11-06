package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleDetailResponseDTO {

    private Long id;

    private String roleName;

    private String roleDescription;

    private String roleFor;

    private String status;

    private List<RolePermissionDTO> menus;
}