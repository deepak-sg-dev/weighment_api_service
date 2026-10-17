package com.suguna.weighment_api_service.admin.dtos.responses;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleMenuAccessResponseDTO {

    private Long id;
    private Long roleId;
    private Long menuId;

    private String canRead;
    private String canWrite;
    private String canUpdate;
    private String canDelete;

    private String status;
}
