package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.CreateDeviceRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateDeviceRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateDeviceStatusRequestDTO;

import com.suguna.weighment_api_service.admin.dtos.responses.CreateDeviceResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.DeviceDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.DeviceListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateDeviceResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateDeviceStatusResponseDTO;

import com.suguna.weighment_api_service.admin.services.DeviceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/devices")
public class DeviceController {

    @Autowired
    private DeviceService service;

    @GetMapping
    public ResponseEntity<DeviceListResponseDTO> getDevices(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long branchId) {

        return ResponseEntity.ok(
                service.getDevices(
                        page,
                        limit,
                        search,
                        branchId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailResponseDTO> getDeviceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getDeviceById(id));
    }

    @PostMapping
    public ResponseEntity<CreateDeviceResponseDTO> createDevice(
            @RequestBody CreateDeviceRequestDTO request) {

        return ResponseEntity.ok(
                service.createDevice(request));
    }

    @PatchMapping("/{deviceId}")
    public ResponseEntity<UpdateDeviceResponseDTO> updateDevice(
            @PathVariable Long deviceId,
            @RequestBody UpdateDeviceRequestDTO request) {

        return ResponseEntity.ok(
                service.updateDevice(
                        deviceId,
                        request));
    }

    @PatchMapping("/{deviceId}/status")
    public ResponseEntity<UpdateDeviceStatusResponseDTO> updateDeviceStatus(
            @PathVariable Long deviceId,
            @RequestBody UpdateDeviceStatusRequestDTO request) {

        return ResponseEntity.ok(
                service.updateDeviceStatus(
                        deviceId,
                        request));
    }
}