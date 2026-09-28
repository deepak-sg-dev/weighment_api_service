package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.config.JwtProperties;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.domain.DeviceRegistration;
import com.suguna.weighment_api_service.domain.SupervisorProfile;
import com.suguna.weighment_api_service.dto.auth.DeviceLoginRequest;
import com.suguna.weighment_api_service.dto.auth.DeviceLoginResponse;
import com.suguna.weighment_api_service.dto.auth.SupervisorDto;
import com.suguna.weighment_api_service.dto.auth.SupervisorMeResponse;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.repository.DeviceRegistry;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.security.JwtService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final DeviceRegistry deviceRegistry;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(DeviceRegistry deviceRegistry, JwtService jwtService, JwtProperties jwtProperties) {
        this.deviceRegistry = deviceRegistry;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public DeviceLoginResponse deviceLogin(DeviceLoginRequest request) {
        DeviceRegistration registration = deviceRegistry
                .findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.DEVICE_NOT_CONFIGURED,
                        "Device is not mapped to a supervisor. Show device ID on activation screen."));

        if (!registration.isAuthorized()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED_DEVICE);
        }
        if (!registration.isEnabled()) {
            throw new ApiException(ErrorCode.DEVICE_DISABLED);
        }

        SupervisorProfile supervisor = registration.getSupervisor();
        if (supervisor == null || !supervisor.isActive()) {
            throw new ApiException(ErrorCode.SUPERVISOR_INACTIVE);
        }

        String accessToken = jwtService.generateAccessToken(registration.getDeviceId(), supervisor);
        return DeviceLoginResponse.builder()
                .success(true)
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpiresInSeconds())
                .supervisor(SupervisorDto.from(supervisor))
                .build();
    }

    public SupervisorMeResponse currentSupervisor(AuthenticatedSupervisor authenticatedSupervisor) {
        return SupervisorMeResponse.builder()
                .success(true)
                .supervisor(SupervisorDto.from(authenticatedSupervisor.getSupervisor()))
                .build();
    }

    public void logout(AuthenticatedSupervisor authenticatedSupervisor, String deviceId) {
        if (!authenticatedSupervisor.getDeviceId().equals(deviceId.trim())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "deviceId does not match authenticated session");
        }
    }
}
