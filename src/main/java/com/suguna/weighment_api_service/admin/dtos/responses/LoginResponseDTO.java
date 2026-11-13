package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {

    private String accessToken;

    private Long expiresIn;

    private String refreshToken;

    private UserDTO user;
}