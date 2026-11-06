package com.suguna.weighment_api_service.admin.controller;



import com.suguna.weighment_api_service.admin.dtos.requests.UserMenuBulkRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserMenuAccessResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.UserMenuAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-menu")
@Tag(name = "User Menu Access API")
public class UserMenuAccessController {

    @Autowired
    private UserMenuAccessService service;

    @Operation(summary = "Assign menu access to user")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> assignMenus(
            @RequestBody UserMenuBulkRequestDTO dto) {
        return Response.success("Menu Assigned successfully", service.assignMenusToUser(dto));
    }

    @Operation(summary = "Get user menu access")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<UserMenuAccessResponseDTO>>> getMenusByUser(
            @PathVariable Long userId) {

        List<UserMenuAccessResponseDTO> userMenuAccessResponseDTOS = service.getMenusByUser(userId);

        return Response.success("Data fetched successfully", userMenuAccessResponseDTOS);

    }

    @Operation(summary = "Update user menu access")
    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateMenus(
            @RequestBody UserMenuBulkRequestDTO dto) {

        return Response.success("User menu updated", service.updateUserMenuAccess(dto));
    }

    @Operation(summary = "Remove user menu access")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {

        return Response.success("Deleted successfully", service.removeUserMenuAccess(id));

    }
}
