package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.UpdateBranchConfigurationRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.*;

public interface ConfigurationService {

    CompanyConfigurationResponseDTO
    getCompanyConfiguration();

    CompanyConfigurationOptionsResponseDTO
    getCompanyConfigurationOptions();

    UpdateResponseDTO
    updateCompanyConfiguration();

    BranchConfigurationResponseDTO
    getBranchConfiguration(Long branchId);

    UpdateResponseDTO
    updateBranchConfiguration(
            Long branchId,
            UpdateBranchConfigurationRequestDTO request);

    BranchConfigurationOptionsResponseDTO
    getBranchConfigurationOptions(Long branchId);

    EffectiveBranchConfigurationResponseDTO
    getEffectiveConfiguration(Long branchId);
}