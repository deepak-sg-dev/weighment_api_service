package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.config.AuthSeedProperties;
import com.suguna.weighment_api_service.domain.DeviceRegistration;
import com.suguna.weighment_api_service.domain.SupervisorProfile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temporary device-to-supervisor store until ERP mapping APIs/tables are wired.
 */
@Repository
public class InMemoryDeviceRegistry implements DeviceRegistry {

    private final Map<String, DeviceRegistration> devices = new ConcurrentHashMap<>();

    public InMemoryDeviceRegistry(AuthSeedProperties seedProperties) {
        seedProperties.getDevices().forEach(entry -> {
            SupervisorProfile supervisor = SupervisorProfile.builder()
                    .id(entry.getSupervisorId())
                    .code(entry.getSupervisorCode())
                    .name(entry.getSupervisorName())
                    .mobile(entry.getSupervisorMobile())
                    .branchId(entry.getBranchId())
                    .branchName(entry.getBranchName())
                    .active(entry.isSupervisorActive())
                    .build();
            DeviceRegistration registration = DeviceRegistration.builder()
                    .deviceId(entry.getDeviceId())
                    .enabled(entry.isEnabled())
                    .authorized(entry.isAuthorized())
                    .supervisor(supervisor)
                    .build();
            devices.put(normalize(entry.getDeviceId()), registration);
        });
    }

    @Override
    public Optional<DeviceRegistration> findByDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(devices.get(normalize(deviceId)));
    }

    private static String normalize(String deviceId) {
        return deviceId.trim();
    }
}
