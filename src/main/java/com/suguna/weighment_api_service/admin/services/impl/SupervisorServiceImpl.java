package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorResponseDTO;
import com.suguna.weighment_api_service.admin.entities.Supervisor;
import com.suguna.weighment_api_service.admin.repositories.SupervisorRepository;
import com.suguna.weighment_api_service.admin.services.SupervisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public SupervisorListResponseDTO getSupervisors(
            Integer page,
            Integer limit,
            String search,
            Long branchId) {

        Pageable pageable =
                PageRequest.of(page - 1, limit);

        Page<Supervisor> supervisors;

        if (search != null
                && !search.trim().isEmpty()) {

            supervisors =
                    supervisorRepository
                            .findByOrganizationIdAndApplicationAndEmpNameContainingIgnoreCase(
                                    branchId,
                                    APPLICATION,
                                    search,
                                    pageable);

        } else {

            supervisors =
                    supervisorRepository
                            .findByOrganizationIdAndApplication(
                                    branchId,
                                    APPLICATION,
                                    pageable);
        }

        List<SupervisorResponseDTO> items =
                supervisors.getContent()
                        .stream()
                        .map(this::mapSupervisor)
                        .toList();

        PaginationDTO pagination =
                new PaginationDTO();

        pagination.setPage(page);
        pagination.setLimit(limit);
        pagination.setTotal(
                supervisors.getTotalElements());

        SupervisorListResponseDTO response =
                new SupervisorListResponseDTO();

        response.setItems(items);
        response.setPagination(
                pagination);

        return response;
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

        return mapSupervisor(
                supervisor);
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