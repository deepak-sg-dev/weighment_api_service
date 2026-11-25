package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.requests.UpdateBranchConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateCompanyConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateRegionConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.*;
import com.suguna.weighment_api_service.admin.entities.AdminConfigEntity;
import com.suguna.weighment_api_service.admin.entities.OrganizationBranch;
import com.suguna.weighment_api_service.admin.repositories.AdminConfigRepository;
import com.suguna.weighment_api_service.admin.repositories.OrganizationBranchRepository;
import com.suguna.weighment_api_service.admin.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private static final String APPLICATION = "BRO-WEIGHMENT";
    private static final String DIVISION = "Broiler";
    private static final String ACTIVE_FLAG = "Y";

    @Autowired
    private OrganizationBranchRepository repository;

    @Autowired
    private AdminConfigRepository adminConfigRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // =========================================================================
    // 1. COMPANY CONFIGURATION
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public CompanyConfigurationResponseDTO getCompanyConfiguration() {
        CompanyConfigurationResponseDTO dto = new CompanyConfigurationResponseDTO();

        dto.setCompanyShortName("SFPL");
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

        String regInfo = repository.getRegisteredOfficeAddress(2025L);
        if (regInfo != null && !regInfo.isBlank()) {
            dto.setCompanyName(extractField(regInfo, "Registered office\\s*:\\s*([^,\\r\\n]+)"));

            int phIndex = regInfo.indexOf("Ph No:");
            if (phIndex > 0) {
                String rawAddress = regInfo.substring(0, phIndex)
                        .replaceFirst("(?i)^Registered office\\s*:\\s*", "")
                        .replaceAll("\\r?\\n", " ")
                        .trim();
                dto.setAddress(rawAddress);
            }

            dto.setCompanyMobile(extractField(regInfo, "Ph No\\s*:\\s*([^,\\r\\n]+)"));
            dto.setCinNumber(extractField(regInfo, "CIN No\\s*:\\s*([^,\\r\\n]+)"));
            dto.setPanNo(extractField(regInfo, "PAN\\s*:\\s*([^,\\r\\n]+)"));
            dto.setCompanyEmail(extractField(regInfo, "Email\\s*:\\s*([^,\\r\\n]+)"));
            dto.setWebsite(extractField(regInfo, "Website\\s*:\\s*([^,\\r\\n]+)"));
            dto.setCustomerCareNo(extractField(regInfo, "Customer Care No\\s*:\\s*([^,\\r\\n]+)"));
        }

        AdminConfigEntity cfg = adminConfigRepository
                .findFirstByApplicationAndRegionIdIsNullAndBranchIdIsNullAndActiveOrderByIdDesc(
                        APPLICATION, ACTIVE_FLAG)
                .orElse(null);

        if (cfg != null) {
            dto.setOperatingStyle(cfg.getOperatingStyle());
            dto.setDefaultUnit(cfg.getDefaultUnit());
            dto.setAllowLameBirds(toBoolean(cfg.getAllowLameBirds()));
            dto.setAllowManualWeighment(toBoolean(cfg.getAllowManualWeighment()));
            dto.setLockAvgWeight(toBoolean(cfg.getLockAvgWeight()));
            dto.setScaleIntegrationEnabled(toBoolean(cfg.getScaleIntegrationEnabled()));
            dto.setScaleConnectionType(cfg.getScaleConnectionType());
            dto.setScaleProtocol(cfg.getScaleProtocol());
            dto.setAutoCaptureStableWeight(toBoolean(cfg.getAutoCaptureStableWeight()));
            dto.setMinWeightThreshold(cfg.getMinWeightThreshold());
            dto.setWeightToleranceType(cfg.getWeightToleranceType());
            dto.setWeightToleranceLimit(cfg.getWeightToleranceLimit());
            dto.setBirdCountTolerance(cfg.getBirdCountTolerance());
            dto.setCrateTareTolerance(cfg.getCrateTareTolerance());
            dto.setEnforceToleranceApproval(toBoolean(cfg.getEnforceToleranceApproval()));
            dto.setDcSigningRequired(toBoolean(cfg.getDcSigningRequired()));
            dto.setOtpEnabled(toBoolean(cfg.getOtpEnabled()));
            dto.setDcPrintingStyle(cfg.getDcPrintingStyle());
            dto.setGpsMandatory(toBoolean(cfg.getGpsMandatory()));
            dto.setGpsVarianceLimit(cfg.getGpsVarianceLimit());
            dto.setManualScheduling(toBoolean(cfg.getManualScheduling()));
            dto.setScheduleLimitingHours(cfg.getScheduleLimitingHours());
            dto.setRemarks(cfg.getRemarks());
        } else {
            dto.setOperatingStyle("NORMAL");
            dto.setDefaultUnit("WEIGHT");
        }

        return dto;
    }

    @Override
    public CompanyConfigurationOptionsResponseDTO getCompanyConfigurationOptions() {
        CompanyConfigurationOptionsResponseDTO dto = new CompanyConfigurationOptionsResponseDTO();
        dto.setOperatingStyles(List.of("NORMAL", "TARE_WEIGHMENT", "FARM_WEIGHT"));
        dto.setDefaultUnits(List.of("WEIGHT"));
        dto.setDcPrintingStyles(List.of("REGULAR_32_COL_FORMAT_2"));
        return dto;
    }

    @Override
    @Transactional
    public UpdateResponseDTO updateCompanyConfiguration(UpdateCompanyConfigurationRequestDTO request) {
        AdminConfigEntity cfg = adminConfigRepository
                .findFirstByApplicationAndRegionIdIsNullAndBranchIdIsNullAndActiveOrderByIdDesc(
                        APPLICATION, ACTIVE_FLAG)
                .orElseGet(() -> createNewConfigEntity(null, null, request.getUpdatedBy()));

        applyCommonUpdates(
                cfg,
                request.getOperatingStyle(),
                request.getDefaultUnit(),
                request.getAllowLameBirds(),
                request.getAllowManualWeighment(),
                request.getLockAvgWeight(),
                request.getScaleIntegrationEnabled(),
                request.getScaleConnectionType(),
                request.getScaleProtocol(),
                request.getAutoCaptureStableWeight(),
                request.getMinWeightThreshold(),
                request.getWeightToleranceType(),
                request.getWeightToleranceLimit(),
                request.getBirdCountTolerance(),
                request.getCrateTareTolerance(),
                request.getEnforceToleranceApproval(),
                request.getDcSigningRequired(),
                request.getOtpEnabled(),
                request.getDcPrintingStyle(),
                request.getNxinfoPassword(),
                request.getSalesHeadPwd(),
                request.getGpsMandatory(),
                request.getGpsVarianceLimit(),
                request.getManualScheduling(),
                request.getScheduleLimitingHours(),
                request.getRemarks(),
                request.getUpdatedBy()
        );

        adminConfigRepository.save(cfg);

        UpdateResponseDTO dto = new UpdateResponseDTO();
        dto.setUpdated(true);
        return dto;
    }

    // =========================================================================
    // 2. REGION CONFIGURATION
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<RegionConfigurationResponseDTO> getRegions() {
        List<OrganizationBranch> branches = repository.findByDivisionNameIgnoreCaseAndRegionIdIsNotNull(DIVISION);

        Map<Long, List<OrganizationBranch>> groupedByRegion = branches.stream()
                .filter(b -> b.getDisableDate() == null)
                .collect(Collectors.groupingBy(OrganizationBranch::getRegionId));

        return groupedByRegion.entrySet().stream()
                .map(entry -> {
                    Long regionId = entry.getKey();
                    List<OrganizationBranch> regionBranches = entry.getValue();
                    OrganizationBranch sample = regionBranches.get(0);
                    AdminConfigEntity cfg = adminConfigRepository
                            .findFirstByApplicationAndRegionIdAndBranchIdIsNullAndActiveOrderByIdDesc(
                                    APPLICATION, regionId, ACTIVE_FLAG)
                            .orElse(null);

                    return mapRegionDto(sample, (long) regionBranches.size(), cfg);
                })
                .sorted(Comparator.comparing(RegionConfigurationResponseDTO::getRegionName,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RegionConfigurationResponseDTO getRegionConfiguration(Long regionId) {
        List<OrganizationBranch> regionBranches = repository.findByDivisionNameIgnoreCaseAndRegionId(DIVISION, regionId);
        if (regionBranches.isEmpty()) {
            throw new RuntimeException("Region not found: " + regionId);
        }

        long activeBranchCount = regionBranches.stream()
                .filter(b -> b.getDisableDate() == null)
                .count();

        AdminConfigEntity cfg = adminConfigRepository
                .findFirstByApplicationAndRegionIdAndBranchIdIsNullAndActiveOrderByIdDesc(
                        APPLICATION, regionId, ACTIVE_FLAG)
                .orElse(null);

        return mapRegionDto(regionBranches.get(0), activeBranchCount, cfg);
    }

    @Override
    @Transactional
    public UpdateResponseDTO updateRegionConfiguration(Long regionId, UpdateRegionConfigurationRequestDTO request) {
        List<OrganizationBranch> regionBranches = repository.findByDivisionNameIgnoreCaseAndRegionId(DIVISION, regionId);
        if (regionBranches.isEmpty()) {
            throw new RuntimeException("Region not found: " + regionId);
        }

        AdminConfigEntity cfg = adminConfigRepository
                .findFirstByApplicationAndRegionIdAndBranchIdIsNullAndActiveOrderByIdDesc(
                        APPLICATION, regionId, ACTIVE_FLAG)
                .orElseGet(() -> createNewConfigEntity(regionId, null, request.getUpdatedBy()));

        applyCommonUpdates(
                cfg,
                request.getOperatingStyle(),
                request.getDefaultUnit(),
                request.getAllowLameBirds(),
                request.getAllowManualWeighment(),
                request.getLockAvgWeight(),
                request.getScaleIntegrationEnabled(),
                request.getScaleConnectionType(),
                request.getScaleProtocol(),
                request.getAutoCaptureStableWeight(),
                request.getMinWeightThreshold(),
                request.getWeightToleranceType(),
                request.getWeightToleranceLimit(),
                request.getBirdCountTolerance(),
                request.getCrateTareTolerance(),
                request.getEnforceToleranceApproval(),
                request.getDcSigningRequired(),
                request.getOtpEnabled(),
                request.getDcPrintingStyle(),
                request.getNxinfoPassword(),
                request.getSalesHeadPwd(),
                request.getGpsMandatory(),
                request.getGpsVarianceLimit(),
                request.getManualScheduling(),
                request.getScheduleLimitingHours(),
                request.getRemarks(),
                request.getUpdatedBy()
        );

        adminConfigRepository.save(cfg);

        UpdateResponseDTO dto = new UpdateResponseDTO();
        dto.setUpdated(true);
        return dto;
    }

    // =========================================================================
    // 3. BRANCH CONFIGURATION
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public BranchConfigurationResponseDTO getBranchConfiguration(Long branchId) {
        OrganizationBranch branch = repository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        AdminConfigEntity cfg = adminConfigRepository
                .findFirstByApplicationAndBranchIdAndActiveOrderByIdDesc(
                        APPLICATION, branchId, ACTIVE_FLAG)
                .orElse(null);

        BranchConfigurationResponseDTO dto = new BranchConfigurationResponseDTO();
        dto.setBranchId(branch.getBranchId());
        dto.setBranchCode(branch.getBranchCode());
        dto.setBranchName(branch.getBranchName());
        dto.setBranchShortName(branch.getBranchShortName());
        dto.setRegionId(branch.getRegionId());
        dto.setRegionName(branch.getRegion());
        dto.setZoneName(branch.getZoneName());
        dto.setAddress(branch.getLocationName());

        dto.setManagerMobile("");
        dto.setDeviceIdentifiers(List.of());
        dto.setUserCategory("VIEW_USER");
        dto.setGstin("");
        dto.setGstinDate(null);
        dto.setNormalBirdExcessAllowedPercent(0);
        dto.setWeakBirdExcessAllowedPercent(0);
        dto.setBankName("");
        dto.setBankBranch("");
        dto.setBankAccountMasked("");
        dto.setBankIfsc("");

        if (cfg != null) {
            dto.setManualScheduleAllowed(toBoolean(cfg.getManualScheduling()));
            dto.setLameBirdsEnabled(toBoolean(cfg.getAllowLameBirds()));
            dto.setOperatingStyle(cfg.getOperatingStyle());
            dto.setDefaultUnit(cfg.getDefaultUnit());
            dto.setScheduleWeightLimitEnabled(cfg.getScheduleLimitingHours() != null && cfg.getScheduleLimitingHours() > 0);
            dto.setLimitingHours(cfg.getScheduleLimitingHours() != null ? cfg.getScheduleLimitingHours() : 0);

            dto.setAllowManualWeighment(toBoolean(cfg.getAllowManualWeighment()));
            dto.setLockAvgWeight(toBoolean(cfg.getLockAvgWeight()));
            dto.setScaleIntegrationEnabled(toBoolean(cfg.getScaleIntegrationEnabled()));
            dto.setScaleConnectionType(cfg.getScaleConnectionType());
            dto.setScaleProtocol(cfg.getScaleProtocol());
            dto.setAutoCaptureStableWeight(toBoolean(cfg.getAutoCaptureStableWeight()));
            dto.setMinWeightThreshold(cfg.getMinWeightThreshold());
            dto.setWeightToleranceType(cfg.getWeightToleranceType());
            dto.setWeightToleranceLimit(cfg.getWeightToleranceLimit());
            dto.setBirdCountTolerance(cfg.getBirdCountTolerance());
            dto.setCrateTareTolerance(cfg.getCrateTareTolerance());
            dto.setEnforceToleranceApproval(toBoolean(cfg.getEnforceToleranceApproval()));
            dto.setDcSigningRequired(toBoolean(cfg.getDcSigningRequired()));
            dto.setOtpEnabled(toBoolean(cfg.getOtpEnabled()));
            dto.setDcPrintingStyle(cfg.getDcPrintingStyle());
            dto.setGpsMandatory(toBoolean(cfg.getGpsMandatory()));
            dto.setGpsVarianceLimit(cfg.getGpsVarianceLimit());
            dto.setRemarks(cfg.getRemarks());
        } else {
            dto.setManualScheduleAllowed(false);
            dto.setLameBirdsEnabled(false);
            dto.setOperatingStyle("NORMAL");
            dto.setDefaultUnit("WEIGHT");
            dto.setScheduleWeightLimitEnabled(false);
            dto.setLimitingHours(0);
        }

        return dto;
    }

    @Override
    @Transactional
    public UpdateResponseDTO updateBranchConfiguration(Long branchId, UpdateBranchConfigurationRequestDTO request) {
        OrganizationBranch branch = repository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        AdminConfigEntity cfg = adminConfigRepository
                .findFirstByApplicationAndBranchIdAndActiveOrderByIdDesc(
                        APPLICATION, branchId, ACTIVE_FLAG)
                .orElseGet(() -> createNewConfigEntity(branch.getRegionId(), branchId, request.getUpdatedBy()));

        if (cfg.getRegionId() == null && branch.getRegionId() != null) {
            cfg.setRegionId(branch.getRegionId());
        }

        applyCommonUpdates(
                cfg,
                request.getOperatingStyle(),
                request.getDefaultUnit(),
                request.getLameBirdsEnabled(),
                request.getAllowManualWeighment(),
                request.getLockAvgWeight(),
                request.getScaleIntegrationEnabled(),
                request.getScaleConnectionType(),
                request.getScaleProtocol(),
                request.getAutoCaptureStableWeight(),
                request.getMinWeightThreshold(),
                request.getWeightToleranceType(),
                request.getWeightToleranceLimit(),
                request.getBirdCountTolerance(),
                request.getCrateTareTolerance(),
                request.getEnforceToleranceApproval(),
                request.getDcSigningRequired(),
                request.getOtpEnabled(),
                request.getDcPrintingStyle(),
                request.getNxinfoPassword(),
                request.getSalesHeadPwd(),
                request.getGpsMandatory(),
                request.getGpsVarianceLimit(),
                request.getManualScheduleAllowed(),
                request.getLimitingHours(),
                request.getRemarks(),
                request.getUpdatedBy()
        );

        adminConfigRepository.save(cfg);

        UpdateResponseDTO dto = new UpdateResponseDTO();
        dto.setUpdated(true);
        return dto;
    }

    @Override
    public BranchConfigurationOptionsResponseDTO getBranchConfigurationOptions(Long branchId) {
        BranchConfigurationOptionsResponseDTO dto = new BranchConfigurationOptionsResponseDTO();
        dto.setOperatingStyles(List.of("NORMAL", "TARE_WEIGHMENT", "FARM_WEIGHT"));
        dto.setUnits(List.of("WEIGHT"));
        dto.setUserCategories(List.of("VIEW_USER"));
        return dto;
    }

    // =========================================================================
    // 4. EFFECTIVE CONFIGURATION (BRANCH -> REGION -> COMPANY -> DEFAULT)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public EffectiveBranchConfigurationResponseDTO getEffectiveConfiguration(Long branchId) {
        OrganizationBranch branch = repository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        AdminConfigEntity companyCfg = adminConfigRepository
                .findFirstByApplicationAndRegionIdIsNullAndBranchIdIsNullAndActiveOrderByIdDesc(
                        APPLICATION, ACTIVE_FLAG)
                .orElse(null);

        AdminConfigEntity regionCfg = branch.getRegionId() != null
                ? adminConfigRepository
                .findFirstByApplicationAndRegionIdAndBranchIdIsNullAndActiveOrderByIdDesc(
                        APPLICATION, branch.getRegionId(), ACTIVE_FLAG)
                .orElse(null)
                : null;

        AdminConfigEntity branchCfg = adminConfigRepository
                .findFirstByApplicationAndBranchIdAndActiveOrderByIdDesc(
                        APPLICATION, branchId, ACTIVE_FLAG)
                .orElse(null);

        Map<String, Object> effective = new LinkedHashMap<>();
        Map<String, String> sources = new LinkedHashMap<>();

        resolveValue("operatingStyle", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getOperatingStyle, "NORMAL");
        resolveValue("defaultUnit", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getDefaultUnit, "WEIGHT");
        resolveBoolValue("allowLameBirds", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getAllowLameBirds, false);
        resolveBoolValue("allowManualWeighment", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getAllowManualWeighment, false);
        resolveBoolValue("lockAvgWeight", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getLockAvgWeight, false);
        resolveBoolValue("scaleIntegrationEnabled", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getScaleIntegrationEnabled, false);
        resolveValue("scaleConnectionType", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getScaleConnectionType, null);
        resolveValue("scaleProtocol", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getScaleProtocol, null);
        resolveBoolValue("autoCaptureStableWeight", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getAutoCaptureStableWeight, false);
        resolveValue("minWeightThreshold", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getMinWeightThreshold, null);
        resolveValue("weightToleranceType", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getWeightToleranceType, null);
        resolveValue("weightToleranceLimit", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getWeightToleranceLimit, null);
        resolveValue("birdCountTolerance", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getBirdCountTolerance, null);
        resolveValue("crateTareTolerance", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getCrateTareTolerance, null);
        resolveBoolValue("enforceToleranceApproval", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getEnforceToleranceApproval, false);
        resolveBoolValue("dcSigningRequired", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getDcSigningRequired, false);
        resolveBoolValue("otpEnabled", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getOtpEnabled, false);
        resolveValue("dcPrintingStyle", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getDcPrintingStyle, "REGULAR_32_COL_FORMAT_2");
        resolveBoolValue("gpsMandatory", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getGpsMandatory, false);
        resolveValue("gpsVarianceLimit", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getGpsVarianceLimit, BigDecimal.ZERO);
        resolveBoolValue("manualScheduling", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getManualScheduling, false);
        resolveValue("scheduleLimitingHours", effective, sources, branchCfg, regionCfg, companyCfg,
                AdminConfigEntity::getScheduleLimitingHours, 0);

        EffectiveBranchConfigurationResponseDTO dto = new EffectiveBranchConfigurationResponseDTO();
        dto.setBranchId(branchId);
        dto.setEffective(effective);
        dto.setSources(sources);
        return dto;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private AdminConfigEntity createNewConfigEntity(Long regionId, Long branchId, Long userId) {
        Long nextId = adminConfigRepository.findTopByOrderByIdDesc()
                .map(last -> last.getId() + 1)
                .orElse(1L);

        AdminConfigEntity entity = new AdminConfigEntity();
        entity.setId(nextId);
        entity.setApplication(APPLICATION);
        entity.setRegionId(regionId);
        entity.setBranchId(branchId);
        entity.setActive(ACTIVE_FLAG);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(userId);
        return entity;
    }

    private void applyCommonUpdates(
            AdminConfigEntity cfg,
            String operatingStyle,
            String defaultUnit,
            Boolean allowLameBirds,
            Boolean allowManualWeighment,
            Boolean lockAvgWeight,
            Boolean scaleIntegrationEnabled,
            String scaleConnectionType,
            String scaleProtocol,
            Boolean autoCaptureStableWeight,
            BigDecimal minWeightThreshold,
            String weightToleranceType,
            BigDecimal weightToleranceLimit,
            Long birdCountTolerance,
            BigDecimal crateTareTolerance,
            Boolean enforceToleranceApproval,
            Boolean dcSigningRequired,
            Boolean otpEnabled,
            String dcPrintingStyle,
            String nxinfoPassword,
            String salesHeadPwd,
            Boolean gpsMandatory,
            BigDecimal gpsVarianceLimit,
            Boolean manualScheduling,
            Integer scheduleLimitingHours,
            String remarks,
            Long updatedBy) {

        if (operatingStyle != null) cfg.setOperatingStyle(operatingStyle);
        if (defaultUnit != null) cfg.setDefaultUnit(defaultUnit);
        if (allowLameBirds != null) cfg.setAllowLameBirds(toFlag(allowLameBirds));
        if (allowManualWeighment != null) cfg.setAllowManualWeighment(toFlag(allowManualWeighment));
        if (lockAvgWeight != null) cfg.setLockAvgWeight(toFlag(lockAvgWeight));
        if (scaleIntegrationEnabled != null) cfg.setScaleIntegrationEnabled(toFlag(scaleIntegrationEnabled));
        if (scaleConnectionType != null) cfg.setScaleConnectionType(scaleConnectionType);
        if (scaleProtocol != null) cfg.setScaleProtocol(scaleProtocol);
        if (autoCaptureStableWeight != null) cfg.setAutoCaptureStableWeight(toFlag(autoCaptureStableWeight));
        if (minWeightThreshold != null) cfg.setMinWeightThreshold(minWeightThreshold);
        if (weightToleranceType != null) cfg.setWeightToleranceType(weightToleranceType);
        if (weightToleranceLimit != null) cfg.setWeightToleranceLimit(weightToleranceLimit);
        if (birdCountTolerance != null) cfg.setBirdCountTolerance(birdCountTolerance);
        if (crateTareTolerance != null) cfg.setCrateTareTolerance(crateTareTolerance);
        if (enforceToleranceApproval != null) cfg.setEnforceToleranceApproval(toFlag(enforceToleranceApproval));
        if (dcSigningRequired != null) cfg.setDcSigningRequired(toFlag(dcSigningRequired));
        if (otpEnabled != null) cfg.setOtpEnabled(toFlag(otpEnabled));
        if (dcPrintingStyle != null) cfg.setDcPrintingStyle(dcPrintingStyle);
        if (nxinfoPassword != null && !nxinfoPassword.isBlank()) {
            cfg.setNxinfoPasswordHash(passwordEncoder.encode(nxinfoPassword));
        }
        if (salesHeadPwd != null && !salesHeadPwd.isBlank()) {
            cfg.setSalesHeadPwdHash(passwordEncoder.encode(salesHeadPwd));
        }
        if (gpsMandatory != null) cfg.setGpsMandatory(toFlag(gpsMandatory));
        if (gpsVarianceLimit != null) cfg.setGpsVarianceLimit(gpsVarianceLimit);
        if (manualScheduling != null) cfg.setManualScheduling(toFlag(manualScheduling));
        if (scheduleLimitingHours != null) cfg.setScheduleLimitingHours(scheduleLimitingHours);
        if (remarks != null) cfg.setRemarks(remarks);

        cfg.setUpdatedAt(LocalDateTime.now());
        if (updatedBy != null) cfg.setUpdatedBy(updatedBy);
    }

    private RegionConfigurationResponseDTO mapRegionDto(
            OrganizationBranch sample,
            Long branchCount,
            AdminConfigEntity cfg) {

        RegionConfigurationResponseDTO dto = new RegionConfigurationResponseDTO();
        dto.setRegionId(sample.getRegionId());
        dto.setRegionCode(sample.getRegionCode());
        dto.setRegionName(sample.getRegion());
        dto.setZoneName(sample.getZoneName());
        dto.setBranchCount(branchCount);

        if (cfg != null) {
            dto.setOperatingStyle(cfg.getOperatingStyle());
            dto.setDefaultUnit(cfg.getDefaultUnit());
            dto.setAllowLameBirds(toBoolean(cfg.getAllowLameBirds()));
            dto.setAllowManualWeighment(toBoolean(cfg.getAllowManualWeighment()));
            dto.setLockAvgWeight(toBoolean(cfg.getLockAvgWeight()));
            dto.setScaleIntegrationEnabled(toBoolean(cfg.getScaleIntegrationEnabled()));
            dto.setScaleConnectionType(cfg.getScaleConnectionType());
            dto.setScaleProtocol(cfg.getScaleProtocol());
            dto.setAutoCaptureStableWeight(toBoolean(cfg.getAutoCaptureStableWeight()));
            dto.setMinWeightThreshold(cfg.getMinWeightThreshold());
            dto.setWeightToleranceType(cfg.getWeightToleranceType());
            dto.setWeightToleranceLimit(cfg.getWeightToleranceLimit());
            dto.setBirdCountTolerance(cfg.getBirdCountTolerance());
            dto.setCrateTareTolerance(cfg.getCrateTareTolerance());
            dto.setEnforceToleranceApproval(toBoolean(cfg.getEnforceToleranceApproval()));
            dto.setDcSigningRequired(toBoolean(cfg.getDcSigningRequired()));
            dto.setOtpEnabled(toBoolean(cfg.getOtpEnabled()));
            dto.setDcPrintingStyle(cfg.getDcPrintingStyle());
            dto.setGpsMandatory(toBoolean(cfg.getGpsMandatory()));
            dto.setGpsVarianceLimit(cfg.getGpsVarianceLimit());
            dto.setManualScheduling(toBoolean(cfg.getManualScheduling()));
            dto.setScheduleLimitingHours(cfg.getScheduleLimitingHours());
            dto.setRemarks(cfg.getRemarks());
        }

        return dto;
    }

    private <T> void resolveValue(
            String key,
            Map<String, Object> effective,
            Map<String, String> sources,
            AdminConfigEntity branchCfg,
            AdminConfigEntity regionCfg,
            AdminConfigEntity companyCfg,
            Function<AdminConfigEntity, T> getter,
            T defaultValue) {

        if (branchCfg != null && getter.apply(branchCfg) != null) {
            effective.put(key, getter.apply(branchCfg));
            sources.put(key, "BRANCH");
        } else if (regionCfg != null && getter.apply(regionCfg) != null) {
            effective.put(key, getter.apply(regionCfg));
            sources.put(key, "REGION");
        } else if (companyCfg != null && getter.apply(companyCfg) != null) {
            effective.put(key, getter.apply(companyCfg));
            sources.put(key, "COMPANY");
        } else {
            effective.put(key, defaultValue);
            sources.put(key, "DEFAULT");
        }
    }

    private void resolveBoolValue(
            String key,
            Map<String, Object> effective,
            Map<String, String> sources,
            AdminConfigEntity branchCfg,
            AdminConfigEntity regionCfg,
            AdminConfigEntity companyCfg,
            Function<AdminConfigEntity, String> getter,
            Boolean defaultValue) {

        if (branchCfg != null && getter.apply(branchCfg) != null) {
            effective.put(key, toBoolean(getter.apply(branchCfg)));
            sources.put(key, "BRANCH");
        } else if (regionCfg != null && getter.apply(regionCfg) != null) {
            effective.put(key, toBoolean(getter.apply(regionCfg)));
            sources.put(key, "REGION");
        } else if (companyCfg != null && getter.apply(companyCfg) != null) {
            effective.put(key, toBoolean(getter.apply(companyCfg)));
            sources.put(key, "COMPANY");
        } else {
            effective.put(key, defaultValue);
            sources.put(key, "DEFAULT");
        }
    }

    private Boolean toBoolean(String flag) {
        if (flag == null) return null;
        return "Y".equalsIgnoreCase(flag.trim());
    }

    private String toFlag(Boolean value) {
        return Boolean.TRUE.equals(value) ? "Y" : "N";
    }

    private String extractField(String source, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(source);
        return matcher.find() ? matcher.group(1).trim() : "";
    }
}
