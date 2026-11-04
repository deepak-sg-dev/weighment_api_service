package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Data;

import java.util.List;

@Data
public class UserContextResponseDTO {

    private Long id;
    private String name;


    private RoleDTO role;
    private List<BranchAccessDTO> branchAccess;
    private List<String> permissions;
    private String scope;
}