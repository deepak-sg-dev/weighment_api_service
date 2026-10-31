package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.UpdateBranchConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.*;
import com.suguna.weighment_api_service.admin.services.ConfigurationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/configuration")
@Tag(
        name = "Branch Configuration",
        description = "Branch configuration and effective configuration APIs"
)
public class ConfigurationController {

    @Autowired
    private ConfigurationService service;

    @GetMapping("/company/options")
    public CompanyConfigurationOptionsResponseDTO
    getCompanyOptions() {

        return service.getCompanyConfigurationOptions();
    }

    @PatchMapping("/company")
    public UpdateResponseDTO
    updateCompanyConfiguration() {

        return service.updateCompanyConfiguration();
    }

    @GetMapping("/branches/{branchId}")
    public BranchConfigurationResponseDTO
    getBranchConfiguration(
            @PathVariable Long branchId) {

        return service.getBranchConfiguration(
                branchId);
    }

    @PatchMapping("/branches/{branchId}")
    public UpdateResponseDTO
    updateBranchConfiguration(
            @PathVariable Long branchId,
            @RequestBody
            UpdateBranchConfigurationRequestDTO request) {

        return service.updateBranchConfiguration(
                branchId,
                request);
    }

    @GetMapping("/branches/{branchId}/options")
    public BranchConfigurationOptionsResponseDTO
    getBranchOptions(
            @PathVariable Long branchId) {

        return service.getBranchConfigurationOptions(
                branchId);
    }

    @GetMapping("/branches/{branchId}/effective")
    public EffectiveBranchConfigurationResponseDTO
    getEffectiveConfiguration(
            @PathVariable Long branchId) {

        return service.getEffectiveConfiguration(
                branchId);
    }
}