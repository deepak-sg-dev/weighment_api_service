package com.suguna.weighment_api_service.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Device login success payload (requirement doc Â§6.1).
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceLoginResponse {

    private boolean success;
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private SupervisorDto supervisor;
}