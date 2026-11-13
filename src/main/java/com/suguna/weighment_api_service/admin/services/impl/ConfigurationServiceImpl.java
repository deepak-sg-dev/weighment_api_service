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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        dto.setCompanyShortName(
                org != null && org.getRegionCode() != null
                        ? org.getRegionCode()
                        : "");

        dto.setCompanyName("");
        dto.setAddress("");
        dto.setPanNo("");
        dto.setPassword("");
        dto.setCompanyMobile("");
        dto.setCompanyEmail("");
        dto.setCompanyImeiId1("");
        dto.setCompanyImeiId2("");
        dto.setCompanyImeiId3("");
        dto.setWebsite("");
        dto.setCustomerCareNo("");
        dto.setCinNumber("");
        dto.setGstin("");
        dto.setHsnCode("01059400");
        dto.setTaxRate(0);
        dto.setBankName("");
        dto.setBankBranch("");
        dto.setBankAccountNo("");
        dto.setBankIfsc("");
        dto.setEffectiveDate("");
        dto.setAutoSiteCode("N");

        // Mapped from Oracle function sug_fnd_pkg.get_reg_off_address(2025)
        String regInfo =
                repository.getRegisteredOfficeAddress(2025L);

        if (regInfo != null && !regInfo.isBlank()) {

            dto.setCompanyName(
                    extractField(regInfo, "Registered office\\s*:\\s*([^,\\r\\n]+)"));

            int phIndex = regInfo.indexOf("Ph No:");
            if (phIndex > 0) {
                String rawAddress = regInfo.substring(0, phIndex)
                        .replaceFirst("(?i)^Registered office\\s*:\\s*", "")
                        .replaceAll("\\r?\\n", " ")
                        .trim();
                dto.setAddress(rawAddress);
            }

            dto.setCompanyMobile(
                    extractField(regInfo, "Ph No\\s*:\\s*([^,\\r\\n]+)"));
            dto.setCinNumber(
                    extractField(regInfo, "CIN No\\s*:\\s*([^,\\r\\n]+)"));
            dto.setPanNo(
                    extractField(regInfo, "PAN\\s*:\\s*([^,\\r\\n]+)"));
            dto.setCompanyEmail(
                    extractField(regInfo, "Email\\s*:\\s*([^,\\r\\n]+)"));
            dto.setWebsite(
                    extractField(regInfo, "Website\\s*:\\s*([^,\\r\\n]+)"));
            dto.setCustomerCareNo(
                    extractField(regInfo, "Customer Care No\\s*:\\s*([^,\\r\\n]+)"));
        }

        return dto;
    }

    private String extractField(String source, String regex) {
        Matcher matcher = Pattern
                .compile(regex, Pattern.CASE_INSENSITIVE)
                .matcher(source);
        return matcher.find() ? matcher.group(1).trim() : "";
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
