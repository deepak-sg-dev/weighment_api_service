package com.suguna.weighment_api_service.admin.controller;


import com.suguna.weighment_api_service.admin.dtos.requests.RoleRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role API", description = "Manage roles")
public class RoleController {

    @Autowired
    private RoleService service;

    @Operation(summary = "Create Role")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponseDTO>> createRole(
            @RequestBody RoleRequestDTO dto) {
        return Response.success("Role created", service.createRole(dto));
    }

    @Operation(summary = "Update Role")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> updateRole(
            @PathVariable Long id,
            @RequestBody RoleRequestDTO dto) {

        return Response.success("Role updated", service.updateRole(id, dto));

    }

    @Operation(summary = "Get Role by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDTO>> getRoleById(
            @PathVariable Long id) {

        return Response.success("Role fetched", service.getRoleById(id));
    }

    @Operation(summary = "Get all roles")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponseDTO>>> getAllRoles() {

        return Response.success("Role fetched", service.getAllRoles());

    }

    @Operation(summary = "Get active roles by application")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<RoleResponseDTO>>> getActiveRoles(
            @RequestParam String application) {

        return Response.success("Role fetched", service.getActiveRoles(application));

    }

    @Operation(summary = "Delete role (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRole(@PathVariable Long id) {

        return Response.success("Role fetched", service.deleteRole(id));

    }
}

