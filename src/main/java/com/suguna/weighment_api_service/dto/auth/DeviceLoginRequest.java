package com.suguna.weighment_api_service.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceLoginRequest {

    @NotBlank(message = "deviceId is required")
    @Size(max = 128, message = "deviceId must be at most 128 characters")
    private String deviceId;

    @NotBlank(message = "appVersion is required")
    @Size(max = 32)
    private String appVersion;

    @NotBlank(message = "platform is required")
    @Size(max = 32)
    private String platform;

    @Size(max = 128)
    private String deviceName;
}
