package com.suguna.weighment_api_service.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequest {

    @NotBlank(message = "deviceId is required")
    @Size(max = 128)
    private String deviceId;
}
