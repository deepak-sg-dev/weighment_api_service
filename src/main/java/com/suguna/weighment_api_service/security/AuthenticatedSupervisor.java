package com.suguna.weighment_api_service.security;

import com.suguna.weighment_api_service.domain.SupervisorProfile;
import org.springframework.security.core.AuthenticatedPrincipal;

public class AuthenticatedSupervisor implements AuthenticatedPrincipal {

    private final String deviceId;
    private final SupervisorProfile supervisor;

    public AuthenticatedSupervisor(String deviceId, SupervisorProfile supervisor) {
        this.deviceId = deviceId;
        this.supervisor = supervisor;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public SupervisorProfile getSupervisor() {
        return supervisor;
    }

    @Override
    public String getName() {
        return supervisor.getId();
    }
}
