package com.suguna.weighment_api_service.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Device login success payload (requirement doc §6.1).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceLoginResponse {

    private boolean success;
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private SupervisorDto supervisor;
}
