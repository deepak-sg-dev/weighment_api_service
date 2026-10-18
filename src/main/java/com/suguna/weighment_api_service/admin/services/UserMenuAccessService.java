package com.suguna.weighment_api_service.admin.services;


import com.suguna.weighment_api_service.admin.dtos.requests.UserMenuBulkRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserMenuAccessResponseDTO;

import java.util.List;

public interface UserMenuAccessService {

    String assignMenusToUser(UserMenuBulkRequestDTO dto);

    List<UserMenuAccessResponseDTO> getMenusByUser(Long userId);

    String updateUserMenuAccess(UserMenuBulkRequestDTO dto);

    String removeUserMenuAccess(Long id);
}