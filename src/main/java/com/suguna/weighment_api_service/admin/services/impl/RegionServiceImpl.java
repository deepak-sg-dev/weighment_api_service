package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RegionDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RegionItemDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RegionListResponseDTO;
import com.suguna.weighment_api_service.admin.entities.OrganizationBranch;
import com.suguna.weighment_api_service.admin.repositories.OrganizationBranchRepository;
import com.suguna.weighment_api_service.admin.services.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private OrganizationBranchRepository repository;

    @Override
    public RegionListResponseDTO getRegions(
            Integer page,
            Integer limit,
            String search,
            Long regionId) {

        List<OrganizationBranch> branches;

        if (regionId != null) {
            branches = repository.findByDivisionNameIgnoreCaseAndRegionId(
                    "Broiler",
                    regionId);
        } else if (search != null && !search.trim().isEmpty()) {
            branches = repository.findByDivisionNameIgnoreCaseAndRegionContainingIgnoreCase(
                    "Broiler",
                    search.trim());
        } else {
            branches = repository.findByDivisionNameIgnoreCaseAndRegionIdIsNotNull(
                    "Broiler");
        }

        // Deduplicate by regionId while preserving order
        Map<Long, RegionItemDTO> uniqueRegions = new LinkedHashMap<>();

        for (OrganizationBranch branch : branches) {
            if (branch.getRegionId() == null) {
                continue;
            }

            RegionItemDTO existing = uniqueRegions.get(branch.getRegionId());

            if (existing == null) {
                RegionItemDTO dto = new RegionItemDTO();
                dto.setId(branch.getRegionId());
                dto.setRegionCode(branch.getRegionCode());
                dto.setName(branch.getRegion());
                dto.setZoneName(branch.getZoneName());
                uniqueRegions.put(branch.getRegionId(), dto);
            } else if (existing.getZoneName() == null && branch.getZoneName() != null) {
                existing.setZoneName(branch.getZoneName());
            }
        }

        List<RegionItemDTO> allItems = new ArrayList<>(uniqueRegions.values());
        long total = allItems.size();

        int fromIndex = Math.min((page - 1) * limit, allItems.size());
        int toIndex = Math.min(fromIndex + limit, allItems.size());
        List<RegionItemDTO> pagedItems = allItems.subList(fromIndex, toIndex);

        PaginationDTO pagination = new PaginationDTO();
        pagination.setPage(page);
        pagination.setLimit(limit);
        pagination.setTotal(total);

        RegionListResponseDTO response = new RegionListResponseDTO();
        response.setItems(pagedItems);
        response.setPagination(pagination);

        return response;
    }

    @Override
    public RegionDetailResponseDTO getRegionById(Long regionId) {

        List<OrganizationBranch> branches =
                repository.findByDivisionNameIgnoreCaseAndRegionId(
                        "Broiler",
                        regionId);

        if (branches == null || branches.isEmpty()) {
            throw new RuntimeException("Region not found");
        }

        OrganizationBranch first = branches.get(0);

        String zoneName = branches.stream()
                .map(OrganizationBranch::getZoneName)
                .filter(z -> z != null && !z.isBlank())
                .findFirst()
                .orElse(null);

        RegionDetailResponseDTO response = new RegionDetailResponseDTO();
        response.setId(first.getRegionId());
        response.setRegionCode(first.getRegionCode());
        response.setRegion(first.getRegion());
        response.setZoneName(zoneName);

        return response;
    }
}
