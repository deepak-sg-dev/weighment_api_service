package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.UpdateBranchConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateCompanyConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateRegionConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.*;

import java.util.List;

public interface ConfigurationService {

    CompanyConfigurationResponseDTO getCompanyConfiguration();

    CompanyConfigurationOptionsResponseDTO getCompanyConfigurationOptions();

    UpdateResponseDTO updateCompanyConfiguration(UpdateCompanyConfigurationRequestDTO request);

    List<RegionConfigurationResponseDTO> getRegions();

    RegionConfigurationResponseDTO getRegionConfiguration(Long regionId);

    UpdateResponseDTO updateRegionConfiguration(Long regionId, UpdateRegionConfigurationRequestDTO request);

    BranchConfigurationResponseDTO getBranchConfiguration(Long branchId);

    UpdateResponseDTO updateBranchConfiguration(Long branchId, UpdateBranchConfigurationRequestDTO request);

    BranchConfigurationOptionsResponseDTO getBranchConfigurationOptions(Long branchId);

    EffectiveBranchConfigurationResponseDTO getEffectiveConfiguration(Long branchId);
}
