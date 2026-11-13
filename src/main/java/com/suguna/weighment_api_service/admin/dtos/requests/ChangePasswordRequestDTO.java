package com.suguna.weighment_api_service.admin.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequestDTO {

    private String oldPassword;

    private String newPassword;
}
