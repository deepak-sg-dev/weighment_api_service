package com.suguna.weighment_api_service.admin.dtos.requests;

import lombok.Getter;
import lombok.Setter;

    @Getter
    @Setter
    public class CreateUserRequestDTO {

        private String username;
        private String name;
        private String mobile;
        private String email;
        private Long roleId;
        private Long branchId;
        private String password;
        private Boolean active;
    }

