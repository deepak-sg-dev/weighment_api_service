package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PermissionResponseDTO {

    private RoleDTO role;
    private List<BranchAccessDTO> branchAccess;
    private List<String> permissions;
    private String scope;

}
