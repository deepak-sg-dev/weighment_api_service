package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.responses.BranchAccessDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.PermissionResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserContextResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.UserContextService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    getPermissions(@PathVariable Long userId) {

        return Response.success(
                "Permissions fetched successfully",
                userContextService.getPermissions(userId)
        );
    }

    @GetMapping("/me/{userId}")
    public ResponseEntity<ApiResponse<UserContextResponseDTO>>
    getUserContext(@PathVariable Long userId) {

        return Response.success(
                "User context fetched successfully",
                userContextService.getUserContext(userId)
        );
    }

    @GetMapping("/branches/{userId}")
    public ResponseEntity<ApiResponse<List<BranchAccessDTO>>>
    getBranches(@PathVariable Long userId) {

        return Response.success(
                "Branches fetched successfully",
                userContextService.getAccessibleBranches(userId)
        );
    }
}