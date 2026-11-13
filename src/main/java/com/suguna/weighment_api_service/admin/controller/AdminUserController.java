package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.responses.UserListItemDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserListResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(
        name = "Admin Users",
        description = "User accounts and access control APIs"
)
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserListResponseDTO>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String status) {

        return Response.success(
                "Users fetched successfully",
                userService.getUsers(page, limit, search, userType, roleId, status)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserListItemDTO>> getUserById(
            @PathVariable Long id) {

        return Response.success(
                "User details fetched successfully",
                userService.getUserById(id)
        );
    }
}
