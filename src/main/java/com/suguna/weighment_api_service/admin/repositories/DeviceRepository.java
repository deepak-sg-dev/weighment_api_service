package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.Device;
import com.suguna.weighment_api_service.admin.entities.DeviceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository
        extends JpaRepository<Device, DeviceId> {

    List<Device> findByApplication(
            String application);

    List<Device> findByApplicationAndOrganizationId(
            String application,
            BigDecimal organizationId);

    Optional<Device> findFirstByDeviceId(
            BigDecimal deviceId);

    boolean existsByDeviceId(
            BigDecimal deviceId);
}