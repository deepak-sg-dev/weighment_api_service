package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.responses.PermissionResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.UserContextService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user-context")
@Tag(
        name = "User Context & Permissions",
        description = "User context, role and permission APIs"
)
public class UserContextController {

    @Autowired
    private UserContextService userContextService;

    @GetMapping("/permissions/{userId}")
    public ResponseEntity<ApiResponse<PermissionResponseDTO>>
    getPermissions(
            @PathVariable Long userId) {

        return Response.success(
                "Permissions fetched successfully",
                userContextService.getPermissions(userId)
        );
    }
}