package com.suguna.weighment_api_service.admin.dtos.requests;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserMenuBulkRequestDTO {

    private Long userId;
    private List<UserMenuAccessRequestDTO> menus;
}

