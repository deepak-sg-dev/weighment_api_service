package com.suguna.weighment_api_service.admin.dtos.responses;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleResponseDTO {

    private Long id;
    private String application;
    private String roleName;
    private String roleDesc;
    private String status;
    private String roleFor;
    private String feedmillRequired;
    private String franchiseeRequired;
}

