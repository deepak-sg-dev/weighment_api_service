package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.BranchDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.BranchItemDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.BranchListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.entities.OrganizationBranch;
import com.suguna.weighment_api_service.admin.repositories.OrganizationBranchRepository;
import com.suguna.weighment_api_service.admin.services.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {

    @Autowired
    private OrganizationBranchRepository repository;

    @Override
    public BranchListResponseDTO getBranches(
            Integer page,
            Integer limit,
            String search,
            Long branchId) {

        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                Sort.by("branchName").ascending()
        );

        Page<OrganizationBranch> branches;

        if (branchId != null) {
            branches = repository
                    .findByDivisionNameIgnoreCaseAndBranchIdAndDisableDateIsNull(
                            "Broiler",
                            branchId,
                            pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            String value = search.trim();
            try {
                Long id = Long.parseLong(value);
                branches = repository
                        .findByDivisionNameIgnoreCaseAndBranchIdAndDisableDateIsNull(
                                "Broiler",
                                id,
                                pageable);
            } catch (NumberFormatException ex) {
                branches = repository
                        .findByDivisionNameIgnoreCaseAndBranchNameContainingIgnoreCaseAndDisableDateIsNull(
                                "Broiler",
                                value,
                                pageable);
            }
        } else {
            branches = repository
                    .findByDivisionNameIgnoreCaseAndDisableDateIsNull(
                            "Broiler",
                            pageable);
        }

        // Build cross-division zone lookup maps from SUG_ORGANIZATION_MV
        Map<Long, String> regionIdZoneMap = new HashMap<>();
        Map<String, String> regionNameZoneMap = new HashMap<>();

        for (OrganizationBranch b : repository.findAll()) {
            if (b.getZoneName() != null && !b.getZoneName().isBlank()) {
                String z = b.getZoneName().trim();
                if (b.getRegionId() != null) {
                    regionIdZoneMap.putIfAbsent(b.getRegionId(), z);
                }
                if (b.getRegion() != null && !b.getRegion().isBlank()) {
                    regionNameZoneMap.putIfAbsent(b.getRegion().trim().toLowerCase(), z);
                }
            }
        }

        List<BranchItemDTO> items =
                branches.getContent()
                        .stream()
                        .map(branch -> {
                            BranchItemDTO dto = new BranchItemDTO();

                            dto.setId(branch.getBranchId());
                            dto.setBranchCode(branch.getBranchCode());
                            dto.setName(branch.getBranchName());
                            dto.setBranchShortName(branch.getBranchShortName());
                            dto.setZoneName(resolveZoneName(branch, regionIdZoneMap, regionNameZoneMap));
                            dto.setRegion(branch.getRegion());
                            dto.setStatus("ACTIVE");

                            return dto;
                        })
                        .collect(Collectors.toList());

        PaginationDTO pagination = new PaginationDTO();
        pagination.setPage(page);
        pagination.setLimit(limit);
        pagination.setTotal(branches.getTotalElements());

        BranchListResponseDTO response = new BranchListResponseDTO();
        response.setItems(items);
        response.setPagination(pagination);

        return response;
    }

    @Override
    public BranchDetailResponseDTO getBranchById(Long branchId) {

        OrganizationBranch branch =
                repository.findById(branchId)
                        .orElseThrow(() -> new RuntimeException("Branch not found"));

        if (!"Broiler".equalsIgnoreCase(branch.getDivisionName()) || branch.getDisableDate() != null) {
            throw new RuntimeException("Branch not found");
        }

        String resolvedZone = branch.getZoneName();
        if (resolvedZone == null || resolvedZone.isBlank()) {
            resolvedZone = repository.findAll().stream()
                    .filter(b -> b.getZoneName() != null && !b.getZoneName().isBlank())
                    .filter(b -> (branch.getRegionId() != null && branch.getRegionId().equals(b.getRegionId()))
                            || (branch.getRegion() != null && branch.getRegion().equalsIgnoreCase(b.getRegion())))
                    .map(b -> b.getZoneName().trim())
                    .findFirst()
                    .orElse(null);
        }

        BranchDetailResponseDTO response = new BranchDetailResponseDTO();
        response.setId(branch.getBranchId());
        response.setBranchCode(branch.getBranchCode());
        response.setBranchName(branch.getBranchName());
        response.setBranchShortName(branch.getBranchShortName());
        response.setZoneName(resolvedZone);
        response.setRegion(branch.getRegion());
        response.setStateName(branch.getStateName());
        response.setLocationName(branch.getLocationName());
        response.setProcessEnabledFlag(branch.getProcessEnabledFlag());
        response.setStatus("ACTIVE");

        return response;
    }

    private String resolveZoneName(
            OrganizationBranch branch,
            Map<Long, String> regionIdZoneMap,
            Map<String, String> regionNameZoneMap) {

        if (branch.getZoneName() != null && !branch.getZoneName().isBlank()) {
            return branch.getZoneName().trim();
        }
        if (branch.getRegionId() != null && regionIdZoneMap.containsKey(branch.getRegionId())) {
            return regionIdZoneMap.get(branch.getRegionId());
        }
        if (branch.getRegion() != null && regionNameZoneMap.containsKey(branch.getRegion().trim().toLowerCase())) {
            return regionNameZoneMap.get(branch.getRegion().trim().toLowerCase());
        }
        return null;
    }
}
