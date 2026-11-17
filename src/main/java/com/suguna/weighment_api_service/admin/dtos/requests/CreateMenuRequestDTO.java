package com.suguna.weighment_api_service.admin.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMenuRequestDTO {

    private String menuName;
    private String menuPath;
    private Long parentId;
    private String menuType;
    private Long seq;
    private String status;
}