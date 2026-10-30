package com.suguna.weighment_api_service.admin.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleMenuBulkRequestDTO {

    private Long roleId;
    private List<RoleMenuAccessRequestDTO> menus;
}

