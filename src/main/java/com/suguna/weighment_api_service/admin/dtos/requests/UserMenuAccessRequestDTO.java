package com.suguna.weighment_api_service.admin.dtos.requests;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMenuAccessRequestDTO {

    private Long menuId;

    private String canRead;
    private String canWrite;
    private String canUpdate;
    private String canDelete;
}

