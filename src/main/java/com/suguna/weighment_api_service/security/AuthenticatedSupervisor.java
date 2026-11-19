package com.suguna.weighment_api_service.security;

import com.suguna.weighment_api_service.domain.SupervisorProfile;
import org.springframework.security.core.AuthenticatedPrincipal;

public class AuthenticatedSupervisor implements AuthenticatedPrincipal {

    private final String deviceId;
    private final SupervisorProfile supervisor;
    private final String userType;
    private final String vehicleNo;

    public AuthenticatedSupervisor(String deviceId, SupervisorProfile supervisor) {
        this(deviceId, supervisor, "APP", null);
    }

    public AuthenticatedSupervisor(String deviceId, SupervisorProfile supervisor, String userType, String vehicleNo) {
        this.deviceId = deviceId;
        this.supervisor = supervisor;
        this.userType = userType == null || userType.isBlank() ? "APP" : userType.trim();
        this.vehicleNo = vehicleNo == null || vehicleNo.isBlank() ? null : vehicleNo.trim();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public SupervisorProfile getSupervisor() {
        return supervisor;
    }

    public String getUserType() {
        return userType;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public boolean isDriver() {
        return "DRIVER".equalsIgnoreCase(userType);
    }

    @Override
    public String getName() {
        return supervisor.getId();
    }
}
