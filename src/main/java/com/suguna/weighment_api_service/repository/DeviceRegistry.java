package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.domain.DeviceRegistration;

import java.util.Optional;

public interface DeviceRegistry {

    Optional<DeviceRegistration> findByDeviceId(String deviceId);
}
