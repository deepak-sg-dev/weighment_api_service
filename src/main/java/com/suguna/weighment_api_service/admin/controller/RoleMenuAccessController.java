package com.suguna.weighment_api_service.admin.controller;


import com.suguna.weighment_api_service.admin.dtos.requests.RoleMenuBulkRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleMenuAccessResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.RoleMenuAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role-menu")
@Tag(name = "Role Menu Access API")
public class RoleMenuAccessController {

    @Autowired
    private RoleMenuAccessService service;

    @Operation(summary = "Assign menus to role")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> assignMenus(
            @RequestBody RoleMenuBulkRequestDTO dto) {

        return Response.success("Menu Created successfully", service.assignMenusToRole(dto));

    }

    @Operation(summary = "Get menus by role")
    @GetMapping("/role/{roleId}")
    public ResponseEntity<ApiResponse<List<RoleMenuAccessResponseDTO>>> getMenusByRole(
            @PathVariable Long roleId) {


        List<RoleMenuAccessResponseDTO> roleMenuAccessResponseDTOList = service.getMenusByRole(roleId);

        return Response.success("Fetched successfully", roleMenuAccessResponseDTOList);
    }

    @Operation(summary = "Update role menu access")
    @PutMapping
    public ResponseEntity<String> updateRoleMenus(
            @RequestBody RoleMenuBulkRequestDTO dto) {

        service.updateRoleMenuAccess(dto);
        return ResponseEntity.ok("Role menu updated successfully");
    }

    @Operation(summary = "Delete role menu mapping")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoleMenu(@PathVariable Long id) {

        service.deleteRoleMenu(id);
        return ResponseEntity.ok("Mapping deleted");
    }
}
