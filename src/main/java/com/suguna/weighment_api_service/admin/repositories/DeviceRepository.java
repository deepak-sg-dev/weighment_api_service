package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository
        extends JpaRepository<Device, Long>,
        JpaSpecificationExecutor<Device> {

    List<Device> findByApplication(
            String application);

    List<Device> findByApplicationAndOrganizationId(
            String application,
            BigDecimal organizationId);

    Optional<Device> findFirstByDeviceId(
            BigDecimal deviceId);

    boolean existsByDeviceId(
            BigDecimal deviceId);

    // NEW METHODS

    Optional<Device> findFirstByAndroidId(
            String androidId);

    boolean existsByAndroidId(
            String androidId);

    Page<Device> findByApplication(
            String application,
            Pageable pageable);

    Page<Device> findByApplicationAndOrganizationId(
            String application,
            BigDecimal organizationId,
            Pageable pageable);

    Page<Device> findByApplicationAndAndroidIdContainingIgnoreCase(
            String application,
            String androidId,
            Pageable pageable);

    Page<Device> findByApplicationAndOrganizationIdAndAndroidIdContainingIgnoreCase(
            String application,
            BigDecimal organizationId,
            String androidId,
            Pageable pageable);
}