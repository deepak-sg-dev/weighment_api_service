package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.SupervisorResponseDTO;
import com.suguna.weighment_api_service.admin.entities.OrganizationMV;
import com.suguna.weighment_api_service.admin.entities.Supervisor;
import com.suguna.weighment_api_service.admin.repositories.OrganizationMVRepository;
import com.suguna.weighment_api_service.admin.repositories.SupervisorRepository;
import com.suguna.weighment_api_service.admin.services.SupervisorService;
import com.suguna.weighment_api_service.admin.specifications.SupervisorSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SupervisorServiceImpl implements SupervisorService {

    private static final String APPLICATION = "BRO-WEIGHMENT";

    @Autowired
    private SupervisorRepository supervisorRepository;

    @Autowired
    private OrganizationMVRepository organizationMVRepository;

    @Override
    public SupervisorListResponseDTO getSupervisors(
            Integer page,
            Integer limit,
            String search,
            Long branchId,
            String status) {

        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                Sort.by(Sort.Direction.ASC, "empName"));

        Page<Supervisor> supervisors = supervisorRepository.findAll(
                SupervisorSpecification.filter(
                        APPLICATION,
                        branchId,
                        "A",
                        search),
                pageable);

        Set<Long> branchIds = supervisors.getContent().stream()
                .map(Supervisor::getOrganizationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> branchNameMap = branchIds.isEmpty()
                ? Collections.emptyMap()
                : organizationMVRepository.findAllById(branchIds).stream()
                .collect(Collectors.toMap(
                        OrganizationMV::getBranchId,
                        OrganizationMV::getBranchName,
                        (existing, replacement) -> existing
                ));

        List<SupervisorResponseDTO> items = supervisors.getContent()
                .stream()
                .map(supervisor -> mapSupervisor(
                        supervisor,
                        branchNameMap.get(supervisor.getOrganizationId())))
                .toList();

        PaginationDTO pagination = new PaginationDTO();
        pagination.setPage(page);
        pagination.setLimit(limit);
        pagination.setTotal(supervisors.getTotalElements());

        SupervisorListResponseDTO response = new SupervisorListResponseDTO();
        response.setItems(items);
        response.setPagination(pagination);

        return response;
    }

    @Override
    public SupervisorResponseDTO getSupervisor(String supervisorId) {
        Supervisor supervisor = supervisorRepository
                .findByEmpCodeAndApplication(supervisorId, APPLICATION)
                .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        String branchName = null;
        if (supervisor.getOrganizationId() != null) {
            branchName = organizationMVRepository.findById(supervisor.getOrganizationId())
                    .map(OrganizationMV::getBranchName)
                    .orElse(null);
        }

        return mapSupervisor(supervisor, branchName);
    }

    private SupervisorResponseDTO mapSupervisor(Supervisor supervisor, String branchName) {
        SupervisorResponseDTO dto = new SupervisorResponseDTO();
        dto.setSupervisorId(supervisor.getEmpCode());
        dto.setName(supervisor.getEmpName());
        dto.setBranchId(supervisor.getOrganizationId());
        dto.setBranchName(branchName);
        dto.setMobile(supervisor.getContactNumber());
        dto.setSupervisorCode(supervisor.getEmpCode());
        dto.setDeviceImei(
                supervisor.getDeviceId() != null
                        ? String.valueOf(supervisor.getDeviceId())
                        : null);
        dto.setStatus(
                "A".equalsIgnoreCase(supervisor.getStatus())
                        ? "ACTIVE"
                        : "INACTIVE");
        return dto;
    }
}
