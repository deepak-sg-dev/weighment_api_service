package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorResponseDTO;
import com.suguna.weighment_api_service.admin.entities.Supervisor;
import com.suguna.weighment_api_service.admin.repositories.SupervisorRepository;
import com.suguna.weighment_api_service.admin.services.SupervisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupervisorServiceImpl
        implements SupervisorService {

    private static final String APPLICATION =
            "BRO-WEIGHMENT";

    @Autowired
    private SupervisorRepository supervisorRepository;

    @Override
    public List<SupervisorResponseDTO> getSupervisors(
            Long branchId) {

        return supervisorRepository
                .findByOrganizationIdAndApplication(
                        branchId,
                        APPLICATION)
                .stream()
                .map(this::mapSupervisor)
                .toList();
    }

    @Override
    public SupervisorResponseDTO getSupervisor(
            String supervisorId) {

        Supervisor supervisor =
                supervisorRepository
                        .findByEmpCodeAndApplication(
                                supervisorId,
                                APPLICATION)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Supervisor not found"));

        return mapSupervisor(supervisor);
    }

    private SupervisorResponseDTO mapSupervisor(
            Supervisor supervisor) {

        SupervisorResponseDTO dto =
                new SupervisorResponseDTO();

        dto.setSupervisorId(
                supervisor.getEmpCode());

        dto.setName(
                supervisor.getEmpName());

        dto.setBranchId(
                supervisor.getOrganizationId());

        dto.setMobile(
                supervisor.getContactNumber());

        dto.setDeviceImei(
                String.valueOf(
                        supervisor.getDeviceId()));

        dto.setStatus(
                supervisor.getStatus());


        return dto;
    }
}