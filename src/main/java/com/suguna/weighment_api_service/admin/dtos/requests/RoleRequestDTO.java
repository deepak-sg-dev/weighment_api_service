package com.suguna.weighment_api_service.admin.dtos.requests;




import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Role request payload")
public class RoleRequestDTO {

    @Schema(example = "POS_APP")
    private String application;

    @Schema(example = "Admin")
    private String roleName;

    @Schema(example = "Suguna Admin")
    private String roleDesc;

    @Schema(example = "EMPLOYEE/CUSTOMER/BOTH")
    private String roleFor;

    @Schema(example = "Y")
    private String feedmillRequired;

    @Schema(example = "Y")
    private String franchiseeRequired;
}

