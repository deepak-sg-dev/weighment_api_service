package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.responses.BranchAccessDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.PermissionResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserContextResponseDTO;

import java.util.List;

public interface UserContextService {

    PermissionResponseDTO getPermissions(Long userId);

    UserContextResponseDTO getUserContext(Long userId);

    List<BranchAccessDTO> getAccessibleBranches(Long userId);
}
