package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.requests.CreateDeviceRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateDeviceRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateDeviceStatusRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.CreateDeviceResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.DeviceDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.DeviceItemDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.DeviceListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateDeviceResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateDeviceStatusResponseDTO;
import com.suguna.weighment_api_service.admin.entities.Device;
import com.suguna.weighment_api_service.admin.repositories.DeviceRepository;
import com.suguna.weighment_api_service.admin.services.DeviceService;
import com.suguna.weighment_api_service.admin.specifications.DeviceSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository repository;

    @Override
    public DeviceListResponseDTO getDevices(
            Integer page,
            Integer limit,
            String search,
            Long branchId) {

        if (page == null || page < 1) {
            page = 1;
        }

        if (limit == null || limit < 1) {
            limit = 20;
        }

        Pageable pageable =
                PageRequest.of(page - 1, limit);

        Page<Device> devices =
                repository.findAll(
                        DeviceSpecification.filter(
                                "WMT_APP",
                                branchId,
                                search),
                        pageable);

        List<DeviceItemDTO> items =
                devices.getContent()
                        .stream()
                        .map(device -> {

                            DeviceItemDTO dto =
                                    new DeviceItemDTO();

                            dto.setId(
                                    device.getAndroidId());

                            dto.setImei(
                                    device.getDeviceId() != null
                                            ? device.getDeviceId().toPlainString()
                                            : null);

                            dto.setBranchId(
                                    device.getOrganizationId() != null
                                            ? device.getOrganizationId().longValue()
                                            : null);

                            dto.setSupervisorId(
                                    device.getEmpId() != null
                                            ? device.getEmpId().longValue()
                                            : null);

                            dto.setStatus(
                                    "A".equalsIgnoreCase(device.getStatus())
                                            ? "ACTIVE"
                                            : "INACTIVE");

                            return dto;
                        })
                        .collect(Collectors.toList());

        PaginationDTO pagination =
                new PaginationDTO();

        pagination.setPage(page);
        pagination.setLimit(limit);
        pagination.setTotal(
                devices.getTotalElements());

        DeviceListResponseDTO response =
                new DeviceListResponseDTO();

        response.setItems(items);
        response.setPagination(pagination);

        return response;
    }

    @Override
    public DeviceDetailResponseDTO getDeviceById(
            Long id) {

        Device device =
                repository.findFirstByDeviceId(
                                BigDecimal.valueOf(id))
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Device not found"));

        DeviceDetailResponseDTO dto =
                new DeviceDetailResponseDTO();

        dto.setId(
                device.getAndroidId());

        dto.setImei(
                device.getDeviceId() != null
                        ? device.getDeviceId().toPlainString()
                        : null);

        dto.setBranchId(
                device.getOrganizationId() != null
                        ? device.getOrganizationId().longValue()
                        : null);

        dto.setSupervisorId(
                device.getEmpId() != null
                        ? device.getEmpId().longValue()
                        : null);

        dto.setStatus(
                "A".equalsIgnoreCase(device.getStatus())
                        ? "ACTIVE"
                        : "INACTIVE");

        return dto;
    }

    @Override

    public CreateDeviceResponseDTO createDevice(
            CreateDeviceRequestDTO request) {

        if (request.getDeviceId() == null ||
                request.getDeviceId().isBlank()) {

            throw new RuntimeException(
                    "Device Id is required");
        }

        if (request.getBranchId() == null) {

            throw new RuntimeException(
                    "Branch Id is required");
        }

        if (request.getSupervisorId() == null) {

            throw new RuntimeException(
                    "Supervisor Id is required");
        }

        if (request.getSupervisorCode() == null ||
                request.getSupervisorCode().isBlank()) {

            throw new RuntimeException(
                    "Supervisor Code is required");
        }

        if (request.getLedgerId() == null) {

            throw new RuntimeException(
                    "Ledger Id is required");
        }

        if (repository.existsByAndroidId(
                request.getDeviceId())) {

            throw new RuntimeException(
                    "Device Id already exists");
        }

        Device device = new Device();

        device.setAndroidId(
                request.getDeviceId());

        if (request.getImei() != null &&
                !request.getImei().isBlank()) {

            try {

                device.setDeviceId(
                        new BigDecimal(
                                request.getImei()));

            } catch (NumberFormatException ex) {

                throw new RuntimeException(
                        "Invalid IMEI");
            }

        } else {

            device.setDeviceId(
                    BigDecimal.ZERO);
        }

        device.setOrganizationId(
                BigDecimal.valueOf(
                        request.getBranchId()));

        device.setEmpCode(
                request.getSupervisorCode());

        device.setApplication(
                "WMT_APP");

        device.setCreatedBy(
                BigDecimal.valueOf(2680));

        device.setLedgerId(
                BigDecimal.valueOf(
                        request.getLedgerId()));

        device.setEmpId(
                BigDecimal.valueOf(
                        request.getSupervisorId()));

        if (request.getSupervisorName() != null) {

            device.setEmpName(
                    request.getSupervisorName());
        }

        device.setStatus("A");

        device.setUserType(
                "EMPLOYEE");

        Date now = new Date();

        device.setCreationDate(now);
        device.setUpdationDate(now);
        device.setLastUpdatedDate(now);

        device.setUpdatedBy(
                BigDecimal.valueOf(2680));

        Device savedDevice =
                repository.save(device);

        CreateDeviceResponseDTO response =
                new CreateDeviceResponseDTO();

        response.setId(
                savedDevice.getDeviceId()
                        .longValue());

        return response;
    }

    @Override
    public UpdateDeviceResponseDTO updateDevice(
            Long deviceId,
            UpdateDeviceRequestDTO request) {

        Device device =
                repository.findFirstByDeviceId(
                                BigDecimal.valueOf(deviceId))
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Device not found"));

        if (request.getBranchId() != null) {

            device.setOrganizationId(
                    BigDecimal.valueOf(
                            request.getBranchId()));
        }

        if (request.getSupervisorId() != null) {

            device.setEmpId(
                    BigDecimal.valueOf(
                            request.getSupervisorId()));
        }

        if (request.getLedgerId() != null) {

            device.setLedgerId(
                    BigDecimal.valueOf(
                            request.getLedgerId()));
        }

        Date now = new Date();

        device.setUpdatedBy(
                BigDecimal.valueOf(2680));

        device.setUpdationDate(now);
        device.setLastUpdatedDate(now);

        repository.save(device);

        UpdateDeviceResponseDTO response =
                new UpdateDeviceResponseDTO();

        response.setUpdated(true);

        return response;
    }

    @Override
    public UpdateDeviceStatusResponseDTO updateDeviceStatus(
            Long deviceId,
            UpdateDeviceStatusRequestDTO request) {

        Device device =
                repository.findFirstByDeviceId(
                                BigDecimal.valueOf(deviceId))
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Device not found"));

        if ("ACTIVE".equalsIgnoreCase(
                request.getStatus())) {

            device.setStatus("A");

        } else if ("INACTIVE".equalsIgnoreCase(
                request.getStatus())) {

            device.setStatus("I");

        } else {

            throw new RuntimeException(
                    "Invalid status");
        }

        Date now = new Date();

        device.setUpdatedBy(
                BigDecimal.valueOf(2680));

        device.setUpdationDate(now);
        device.setLastUpdatedDate(now);

        repository.save(device);

        UpdateDeviceStatusResponseDTO response =
                new UpdateDeviceStatusResponseDTO();

        response.setUpdated(true);

        return response;
    }
}