package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.config.JwtProperties;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.domain.DeviceRegistration;
import com.suguna.weighment_api_service.domain.SupervisorProfile;
import com.suguna.weighment_api_service.dto.auth.DeviceLoginRequest;
import com.suguna.weighment_api_service.dto.auth.DeviceLoginResponse;
import com.suguna.weighment_api_service.dto.auth.LogoutRequest;
import com.suguna.weighment_api_service.dto.auth.SupervisorDto;
import com.suguna.weighment_api_service.dto.auth.SupervisorMeResponse;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.integration.oracle.DeviceLoginSqlClient;
import com.suguna.weighment_api_service.repository.DeviceRegistry;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final DeviceRegistry deviceRegistry;
    private final DeviceLoginSqlClient deviceLoginSqlClient;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(
            DeviceRegistry deviceRegistry,
            DeviceLoginSqlClient deviceLoginSqlClient,
            JwtService jwtService,
            JwtProperties jwtProperties) {
        this.deviceRegistry = deviceRegistry;
        this.deviceLoginSqlClient = deviceLoginSqlClient;
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
        recordLoginAudit(request, supervisor.getCode());
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

    public void logout(AuthenticatedSupervisor authenticatedSupervisor, LogoutRequest request) {
        String deviceId = request.getDeviceId().trim();
        if (!authenticatedSupervisor.getDeviceId().equals(deviceId)) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "deviceId does not match authenticated session");
        }
        log.info(
                "device logout deviceId={} supervisorCode={} latitude={} longitude={}",
                deviceId,
                authenticatedSupervisor.getSupervisor().getCode(),
                request.resolvedLatitude(),
                request.resolvedLongitude());
    }

    private void recordLoginAudit(DeviceLoginRequest request, String supervisorCode) {
        Double lat = request.resolvedLatitude();
        Double lon = request.resolvedLongitude();
        log.info(
                "device login deviceId={} supervisorCode={} platform={} appVersion={} "
                        + "deviceManufacturer={} osVersion={} latitude={} longitude={}",
                request.getDeviceId(),
                supervisorCode,
                request.getPlatform(),
                request.getAppVersion(),
                request.getDeviceManufacturer(),
                request.getOsVersion(),
                lat,
                lon);
        try {
            deviceLoginSqlClient.recordSuccessfulLogin(
                    request.getDeviceId(),
                    request.getOsVersion(),
                    buildDeviceInfoSummary(request));
        } catch (RuntimeException ex) {
            log.warn(
                    "Could not update ERP device login metadata for deviceId={}: {}",
                    request.getDeviceId(),
                    ex.getMessage());
        }
    }

    private static String buildDeviceInfoSummary(DeviceLoginRequest request) {
        StringBuilder summary = new StringBuilder();
        appendPart(summary, request.getDeviceManufacturer());
        appendPart(summary, request.getDeviceName());
        if (request.getAppVersion() != null && !request.getAppVersion().isBlank()) {
            if (!summary.isEmpty()) {
                summary.append('|');
            }
            summary.append("app=").append(request.getAppVersion().trim());
        }
        if (request.getPlatform() != null && !request.getPlatform().isBlank()) {
            if (!summary.isEmpty()) {
                summary.append('|');
            }
            summary.append("platform=").append(request.getPlatform().trim());
        }
        return summary.isEmpty() ? null : summary.toString();
    }

    private static void appendPart(StringBuilder summary, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!summary.isEmpty()) {
            summary.append(' ');
        }
        summary.append(value.trim());
    }
}
