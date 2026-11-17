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

import java.util.List;
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

        } else if (search != null
                && !search.trim().isEmpty()) {

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

        List<BranchItemDTO> items =
                branches.getContent()
                        .stream()
                        .map(branch -> {

                            BranchItemDTO dto =
                                    new BranchItemDTO();

                            dto.setId(branch.getBranchId());

                            dto.setBranchCode(
                                    branch.getBranchCode());

                            dto.setName(
                                    branch.getBranchName());

                            dto.setBranchShortName(
                                    branch.getBranchShortName());

                            dto.setZoneName(
                                    branch.getZoneName());

                            dto.setRegion(
                                    branch.getRegion());

                            dto.setStatus("ACTIVE");

                            return dto;
                        })
                        .collect(Collectors.toList());

        PaginationDTO pagination =
                new PaginationDTO();

        pagination.setPage(page);
        pagination.setLimit(limit);
        pagination.setTotal(
                branches.getTotalElements());

        BranchListResponseDTO response =
                new BranchListResponseDTO();

        response.setItems(items);
        response.setPagination(pagination);

        return response;
    }

    @Override
    public BranchDetailResponseDTO getBranchById(
            Long branchId) {

        OrganizationBranch branch =
                repository.findById(branchId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Branch not found"));

        if (!"Broiler".equalsIgnoreCase(
                branch.getDivisionName())) {

            throw new RuntimeException(
                    "Branch not found");
        }

        if (branch.getDisableDate() != null) {

            throw new RuntimeException(
                    "Branch not found");
        }

        BranchDetailResponseDTO response =
                new BranchDetailResponseDTO();

        response.setId(
                branch.getBranchId());

        response.setBranchCode(
                branch.getBranchCode());

        response.setBranchName(
                branch.getBranchName());

        response.setBranchShortName(
                branch.getBranchShortName());

        response.setZoneName(
                branch.getZoneName());

        response.setRegion(
                branch.getRegion());

        response.setStateName(
                branch.getStateName());

        response.setLocationName(
                branch.getLocationName());

        response.setProcessEnabledFlag(
                branch.getProcessEnabledFlag());

        response.setStatus("ACTIVE");

        return response;
    }
}