package com.suguna.weighment_api_service.admin.services;


import com.suguna.weighment_api_service.admin.dtos.requests.RoleMenuBulkRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleMenuAccessResponseDTO;

import java.util.List;

public interface RoleMenuAccessService {

    String assignMenusToRole(RoleMenuBulkRequestDTO dto);

    List<RoleMenuAccessResponseDTO> getMenusByRole(Long roleId);

    void updateRoleMenuAccess(RoleMenuBulkRequestDTO dto);

    void deleteRoleMenu(Long id);
}