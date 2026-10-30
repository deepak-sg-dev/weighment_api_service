package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.CreateDeviceRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateDeviceRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateDeviceStatusRequestDTO;

import com.suguna.weighment_api_service.admin.dtos.responses.CreateDeviceResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.DeviceDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.DeviceListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateDeviceResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateDeviceStatusResponseDTO;

public interface DeviceService {

    DeviceListResponseDTO getDevices(
            Integer page,
            Integer limit,
            String search,
            Long branchId);

    DeviceDetailResponseDTO getDeviceById(
            Long id);

    CreateDeviceResponseDTO createDevice(
            CreateDeviceRequestDTO request);

    UpdateDeviceResponseDTO updateDevice(
            Long deviceId,
            UpdateDeviceRequestDTO request);

    UpdateDeviceStatusResponseDTO updateDeviceStatus(
            Long deviceId,
            UpdateDeviceStatusRequestDTO request);
}