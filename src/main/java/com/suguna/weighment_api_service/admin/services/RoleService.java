package com.suguna.weighment_api_service.admin.services;



import com.suguna.weighment_api_service.admin.dtos.requests.RoleRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleResponseDTO;

import java.util.List;

public interface RoleService {

    RoleResponseDTO createRole(RoleRequestDTO dto);

    RoleResponseDTO updateRole(Long id, RoleRequestDTO dto);

    RoleResponseDTO getRoleById(Long id);

    List<RoleResponseDTO> getAllRoles();

    List<RoleResponseDTO> getActiveRoles(String application);

    String deleteRole(Long id);
}