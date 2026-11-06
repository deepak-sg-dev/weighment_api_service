package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.CreateRoleRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.CreateRoleResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateRolePermissionsRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateRoleRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateResponseDTO;


public interface RoleService {

    CreateRoleResponseDTO createRole(
            CreateRoleRequestDTO request);

    RoleListResponseDTO getRoles();

    MenuListResponseDTO getMenus();

    RoleDetailResponseDTO getRole(
            Long roleId);
    UpdateResponseDTO updateRole(
            Long roleId,
            UpdateRoleRequestDTO request);

    UpdateResponseDTO updatePermissions(
            Long roleId,
            UpdateRolePermissionsRequestDTO request);
}