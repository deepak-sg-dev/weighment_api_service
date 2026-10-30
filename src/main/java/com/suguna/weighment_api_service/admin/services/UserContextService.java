package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.responses.PermissionResponseDTO;

public interface UserContextService {

    PermissionResponseDTO getPermissions(
            Long userId);
}