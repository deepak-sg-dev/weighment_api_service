package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.CreateRoleRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.CreateRoleResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleListResponseDTO;
import com.suguna.weighment_api_service.admin.services.RoleService;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateRolePermissionsRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateRoleRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/roles")
@Tag(
        name = "Role Management",
        description = "Role and menu permission management APIs"
)
public class RoleController {

    @Autowired
    private RoleService service;

    @PostMapping
    public CreateRoleResponseDTO createRole(
            @RequestBody CreateRoleRequestDTO request) {

        return service.createRole(request);
    }

    @GetMapping
    public RoleListResponseDTO getRoles() {

        return service.getRoles();
    }

    @GetMapping("/menus")
    public MenuListResponseDTO getMenus() {

        return service.getMenus();
    }

    @GetMapping("/{roleId}")
    public RoleDetailResponseDTO getRole(
            @PathVariable Long roleId) {

        return service.getRole(roleId);
    }
    @PatchMapping("/{roleId}")
    public UpdateResponseDTO updateRole(
            @PathVariable Long roleId,
            @RequestBody UpdateRoleRequestDTO request) {

        return service.updateRole(
                roleId,
                request);
    }

    @PatchMapping("/{roleId}/permissions")
    public UpdateResponseDTO updatePermissions(
            @PathVariable Long roleId,
            @RequestBody
            UpdateRolePermissionsRequestDTO request) {

        return service.updatePermissions(
                roleId,
                request);
    }
}