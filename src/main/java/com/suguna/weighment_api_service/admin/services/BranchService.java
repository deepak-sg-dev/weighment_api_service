package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.responses.BranchDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.BranchListResponseDTO;

public interface BranchService {

    BranchListResponseDTO getBranches(
            Integer page,
            Integer limit,
            String search,
            Long branchId);

    BranchDetailResponseDTO getBranchById(Long branchId);
}