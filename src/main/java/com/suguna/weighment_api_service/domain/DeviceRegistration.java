package com.suguna.weighment_api_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DeviceRegistration {

    private final String deviceId;
    private final boolean enabled;
    private final boolean authorized;
    private final SupervisorProfile supervisor;
}
