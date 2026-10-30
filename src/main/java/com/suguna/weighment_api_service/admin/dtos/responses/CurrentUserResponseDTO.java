package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CurrentUserResponseDTO {

    private Long userId;
    private String username;
    private String name;

    private Long roleId;
    private String roleName;

    private Long orgId;
    private String userType;
    private String mobileNumber;

    private String status;

    private List<MenuDTO> menus;
}