package com.suguna.weighment_api_service.admin.dtos.requests;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Menu request payload")
public class MenuRequestDTO {

    @Schema(example = "POS_APP")
    private String application;

    @Schema(example = "Billing")
    private String menuName;

    @Schema(example = "/billing")
    private String menuPath;

    @Schema(example = "CUSTOMER/EMPLOYEE/BOTH")
    private String menuFor;

    private Long parentId;

    private String menuType;

}
