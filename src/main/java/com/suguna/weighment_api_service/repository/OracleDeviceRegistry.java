package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.domain.DeviceRegistration;
import com.suguna.weighment_api_service.integration.oracle.DeviceLoginSqlClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OracleDeviceRegistry implements DeviceRegistry {

    private final DeviceLoginSqlClient deviceLoginSqlClient;

    public OracleDeviceRegistry(DeviceLoginSqlClient deviceLoginSqlClient) {
        this.deviceLoginSqlClient = deviceLoginSqlClient;
    }

    @Override
    public Optional<DeviceRegistration> findByDeviceId(String deviceId) {
        return deviceLoginSqlClient.findDeviceRegistration(deviceId);
    }
}
