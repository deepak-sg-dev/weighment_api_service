package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.UpdateBranchConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateCompanyConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateRegionConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.*;
import com.suguna.weighment_api_service.admin.services.ConfigurationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/configuration")
@Tag(
        name = "Company, Region & Branch Configuration",
        description = "Company, region, and branch configuration APIs"
)
public class ConfigurationController {

    @Autowired
    private ConfigurationService service;

    @GetMapping("/company")
    public CompanyConfigurationResponseDTO getCompanyConfiguration() {
        return service.getCompanyConfiguration();
    }

    @GetMapping("/company/options")
    public CompanyConfigurationOptionsResponseDTO getCompanyOptions() {
        return service.getCompanyConfigurationOptions();
    }

    @PatchMapping("/company")
    public UpdateResponseDTO updateCompanyConfiguration(
            @RequestBody(required = false) UpdateCompanyConfigurationRequestDTO request) {
        return service.updateCompanyConfiguration(
                request != null ? request : new UpdateCompanyConfigurationRequestDTO()
        );
    }

    @GetMapping("/regions")
    public List<RegionConfigurationResponseDTO> getRegions() {
        return service.getRegions();
    }

    @GetMapping("/regions/{regionId}")
    public RegionConfigurationResponseDTO getRegionConfiguration(@PathVariable Long regionId) {
        return service.getRegionConfiguration(regionId);
    }

    @PatchMapping("/regions/{regionId}")
    public UpdateResponseDTO updateRegionConfiguration(
            @PathVariable Long regionId,
            @RequestBody UpdateRegionConfigurationRequestDTO request) {
        return service.updateRegionConfiguration(regionId, request);
    }

    @GetMapping("/branches/{branchId}")
    public BranchConfigurationResponseDTO getBranchConfiguration(@PathVariable Long branchId) {
        return service.getBranchConfiguration(branchId);
    }

    @PatchMapping("/branches/{branchId}")
    public UpdateResponseDTO updateBranchConfiguration(
            @PathVariable Long branchId,
            @RequestBody UpdateBranchConfigurationRequestDTO request) {
        return service.updateBranchConfiguration(branchId, request);
    }

    @GetMapping("/branches/{branchId}/options")
    public BranchConfigurationOptionsResponseDTO getBranchOptions(@PathVariable Long branchId) {
        return service.getBranchConfigurationOptions(branchId);
    }

    @GetMapping("/branches/{branchId}/effective")
    public EffectiveBranchConfigurationResponseDTO getEffectiveConfiguration(@PathVariable Long branchId) {
        return service.getEffectiveConfiguration(branchId);
    }
}
