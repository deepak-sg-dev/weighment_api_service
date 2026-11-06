package com.suguna.weighment_api_service.admin.dtos.requests;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleMenuAccessRequestDTO {

    @Schema(example = "1")
    private Long roleId;

    @Schema(example = "1")
    private Long menuId;

    @Schema(example = "Y")
    private String canRead;

    @Schema(example = "Y")
    private String canWrite;

    @Schema(example = "Y")
    private String canUpdate;

    @Schema(example = "N")
    private String canDelete;
}