package com.suguna.weighment_api_service.admin.dtos.responses;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Menu response")
public class MenuResponseDTO {

    private Long id;
    private String application;
    private String menuName;
    private String menuPath;
    private String menuFor;
    private Long parentId;
    private String status;
    private String menuType;

    private boolean canRead;
    private boolean canWrite;
    private boolean canDelete;
    private boolean canUpdate;

    private boolean assigned;

}
