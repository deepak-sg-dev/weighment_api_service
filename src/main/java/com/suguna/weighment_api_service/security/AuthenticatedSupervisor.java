package com.suguna.weighment_api_service.security;

import com.suguna.weighment_api_service.domain.SupervisorProfile;
import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;

@Getter
public class AuthenticatedSupervisor implements AuthenticatedPrincipal {

    private final String deviceId;
    private final SupervisorProfile supervisor;

    public AuthenticatedSupervisor(String deviceId, SupervisorProfile supervisor) {
        this.deviceId = deviceId;
        this.supervisor = supervisor;
    }

    @Override
    public String getName() {
        return supervisor.getId();
    }
}
