package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.*;
import com.suguna.weighment_api_service.admin.entities.OrganizationBranch;
import com.suguna.weighment_api_service.admin.repositories.OrganizationBranchRepository;
import com.suguna.weighment_api_service.admin.services.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private OrganizationBranchRepository repository;

    private static final List<String> ZONE_DIVISIONS = List.of(
            "Feed",
            "Suguna Hub",
            "Hatchery",
            "Breeder",
            "Breeder & Hatchery",
            "Layer",
            "Layer Hatchery",
            "Layer Parent",
            "Procurement",
            "Regional Office",
            "Feed Sales",
            "Central Store",
            "Corporate Office",
            "Transport",
            "Trading",
            "General",
            "Head Office",
            "Lab",
            "Grain",
            "Globion",
            "Suguna Holdings"
    );

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
        } else {
            branches = repository.findByDivisionNameIgnoreCaseAndRegionIdIsNotNull(
                    "Broiler");
        }

        // Cross-division zone lookup using existing findByDivisionNameIgnoreCase method
        Map<Long, String> regionIdZoneMap = new HashMap<>();
        Map<String, String> regionNameZoneMap = new HashMap<>();

        for (String division : ZONE_DIVISIONS) {
            for (OrganizationBranch b : repository.findByDivisionNameIgnoreCase(division)) {
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
                dto.setZoneName(resolveZoneName(branch, regionIdZoneMap, regionNameZoneMap));
                uniqueRegions.put(branch.getRegionId(), dto);
            } else if (existing.getZoneName() == null || existing.getZoneName().isBlank()) {
                String resolved = resolveZoneName(branch, regionIdZoneMap, regionNameZoneMap);
                if (resolved != null) {
                    existing.setZoneName(resolved);
                }
            }
        }

        List<RegionItemDTO> allItems = new ArrayList<>(uniqueRegions.values());

        // Filter by Region ID, Region Code, Region Name, or Zone Name
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim().toLowerCase();
            allItems = allItems.stream()
                    .filter(item ->
                            (item.getId() != null && String.valueOf(item.getId()).contains(query)) ||
                                    (item.getRegionCode() != null && item.getRegionCode().toLowerCase().contains(query)) ||
                                    (item.getName() != null && item.getName().toLowerCase().contains(query)) ||
                                    (item.getZoneName() != null && item.getZoneName().toLowerCase().contains(query))
                    )
                    .toList();
        }

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
                .orElseGet(() -> {
                    for (String division : ZONE_DIVISIONS) {
                        for (OrganizationBranch b : repository.findByDivisionNameIgnoreCase(division)) {
                            if (b.getZoneName() != null && !b.getZoneName().isBlank()) {
                                if (regionId.equals(b.getRegionId())
                                        || (first.getRegion() != null && first.getRegion().equalsIgnoreCase(b.getRegion()))) {
                                    return b.getZoneName().trim();
                                }
                            }
                        }
                    }
                    return fallbackZoneByRegionName(first.getRegion());
                });

        RegionDetailResponseDTO response = new RegionDetailResponseDTO();
        response.setId(first.getRegionId());
        response.setRegionCode(first.getRegionCode());
        response.setRegion(first.getRegion());
        response.setZoneName(zoneName);

        final String finalZoneName = zoneName;

        List<BranchItemDTO> branchItems =
                branches.stream()
                        .map(branch -> {
                            BranchItemDTO dto = new BranchItemDTO();

                            dto.setId(branch.getBranchId());
                            dto.setBranchCode(branch.getBranchCode());
                            dto.setName(branch.getBranchName());
                            dto.setBranchShortName(branch.getBranchShortName());
                            dto.setZoneName(
                                    (branch.getZoneName() != null && !branch.getZoneName().isBlank())
                                            ? branch.getZoneName().trim()
                                            : finalZoneName
                            );
                            dto.setRegion(branch.getRegion());
                            dto.setStatus(
                                    branch.getDisableDate() == null
                                            ? "ACTIVE"
                                            : "INACTIVE"
                            );

                            return dto;
                        })
                        .toList();

        response.setBranches(branchItems);

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
        if (branch.getRegion() != null) {
            String key = branch.getRegion().trim().toLowerCase();
            if (regionNameZoneMap.containsKey(key)) {
                return regionNameZoneMap.get(key);
            }
            return fallbackZoneByRegionName(branch.getRegion());
        }
        return null;
    }

    private String fallbackZoneByRegionName(String regionName) {
        if (regionName == null) {
            return null;
        }
        return switch (regionName.trim().toLowerCase()) {
            case "north bengal", "south bengal" -> "EAST ZONE";
            case "karnataka2" -> "SOUTH ZONE";
            case "uttarakhand", "uttar pradesh2" -> "NORTH ZONE";
            default -> null;
        };
    }
}
