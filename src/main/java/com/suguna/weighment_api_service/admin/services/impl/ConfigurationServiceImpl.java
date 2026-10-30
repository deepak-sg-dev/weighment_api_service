package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.requests.UpdateBranchConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.*;
import com.suguna.weighment_api_service.admin.entities.OrganizationBranch;
import com.suguna.weighment_api_service.admin.repositories.OrganizationBranchRepository;
import com.suguna.weighment_api_service.admin.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigurationServiceImpl
        implements ConfigurationService {

    @Autowired
    private OrganizationBranchRepository repository;

    @Override
    public CompanyConfigurationResponseDTO
    getCompanyConfiguration() {

        List<OrganizationBranch> branches =
                repository.findByDivisionNameIgnoreCase(
                        "Broiler");

        OrganizationBranch org =
                branches.isEmpty()
                        ? null
                        : branches.get(0);

        CompanyConfigurationResponseDTO dto =
                new CompanyConfigurationResponseDTO();

        if (org != null) {
            dto.setZoneName(
                    org.getZoneName());
        }

        dto.setCompanyShortName("");
        dto.setAddress("");

        dto.setPanNo("");
        dto.setZoneMobile("");
        dto.setZoneEmail("");

        dto.setWebsite("");
        dto.setCustomerCareNo("");

        dto.setCinNumber("");
        dto.setGstin("");

        dto.setBankName("");
        dto.setBankBranch("");
        dto.setBankIfsc("");

        dto.setOtpEnabled(false);
        dto.setGpsMandatory(false);
        dto.setDcSigningEnabled(false);

        return dto;
    }

    @Override
    public CompanyConfigurationOptionsResponseDTO
    getCompanyConfigurationOptions() {

        CompanyConfigurationOptionsResponseDTO dto =
                new CompanyConfigurationOptionsResponseDTO();

        dto.setOperatingStyles(
                List.of(
                        "NORMAL",
                        "TARE_WEIGHMENT",
                        "FARM_WEIGHT"
                ));

        dto.setDefaultUnits(
                List.of(
                        "WEIGHT"
                ));

        dto.setDcPrintingStyles(
                List.of(
                        "REGULAR_32_COL_FORMAT_2"
                ));

        return dto;
    }

    @Override
    public UpdateResponseDTO
    updateCompanyConfiguration() {

        UpdateResponseDTO dto =
                new UpdateResponseDTO();

        dto.setUpdated(true);

        return dto;
    }

    @Override
    public BranchConfigurationResponseDTO
    getBranchConfiguration(Long branchId) {

        OrganizationBranch branch =
                repository.findById(branchId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Branch not found"));

        BranchConfigurationResponseDTO dto =
                new BranchConfigurationResponseDTO();

        dto.setBranchId(
                branch.getBranchId());

        dto.setBranchName(
                branch.getBranchName());

        dto.setAddress(
                branch.getLocationName());

        dto.setManagerMobile("");
        dto.setManualScheduleAllowed(false);
        dto.setLameBirdsEnabled(false);
        dto.setOperatingStyle("NORMAL");

        dto.setDeviceIdentifiers(
                List.of());

        dto.setDefaultUnit(
                "WEIGHT");

        dto.setUserCategory(
                "VIEW_USER");

        dto.setGstin("");
        dto.setGstinDate(null);

        dto.setScheduleWeightLimitEnabled(
                false);

        dto.setLimitingHours(0);

        dto.setNormalBirdExcessAllowedPercent(
                0);

        dto.setWeakBirdExcessAllowedPercent(
                0);

        dto.setBankName("");
        dto.setBankBranch("");
        dto.setBankAccountMasked("");
        dto.setBankIfsc("");

        return dto;
    }

    @Override
    public UpdateResponseDTO
    updateBranchConfiguration(
            Long branchId,
            UpdateBranchConfigurationRequestDTO request) {

        repository.findById(branchId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Branch not found"));

        UpdateResponseDTO dto =
                new UpdateResponseDTO();

        dto.setUpdated(true);

        return dto;
    }

    @Override
    public BranchConfigurationOptionsResponseDTO
    getBranchConfigurationOptions(
            Long branchId) {

        BranchConfigurationOptionsResponseDTO dto =
                new BranchConfigurationOptionsResponseDTO();

        dto.setOperatingStyles(
                List.of(
                        "NORMAL",
                        "TARE_WEIGHMENT",
                        "FARM_WEIGHT"
                ));

        dto.setUnits(
                List.of(
                        "WEIGHT"
                ));

        dto.setUserCategories(
                List.of(
                        "VIEW_USER"
                ));

        return dto;
    }

    @Override
    public EffectiveBranchConfigurationResponseDTO
    getEffectiveConfiguration(
            Long branchId) {

        repository.findById(branchId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Branch not found"));

        EffectiveBranchConfigurationResponseDTO dto =
                new EffectiveBranchConfigurationResponseDTO();

        dto.setBranchId(
                branchId);

        Map<String, Object> effective =
                new HashMap<>();

        effective.put(
                "operatingStyle",
                "NORMAL");

        effective.put(
                "otpEnabled",
                true);

        dto.setEffective(
                effective);

        Map<String, String> sources =
                new HashMap<>();

        sources.put(
                "operatingStyle",
                "BRANCH");

        sources.put(
                "otpEnabled",
                "COMPANY");

        dto.setSources(
                sources);

        return dto;
    }
}