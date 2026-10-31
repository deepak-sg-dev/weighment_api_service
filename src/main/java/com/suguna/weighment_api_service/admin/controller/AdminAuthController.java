package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.LoginRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.CurrentUserResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.LoginResponseDTO;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.dtos.requests.ForgotPasswordRequestDTO;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@Tag(
        name = "Admin Authentication",
        description = "Admin authentication and user management APIs"
)
public class AdminAuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @RequestBody LoginRequestDTO request) {

        return Response.success(
                "Login successful",
                authService.login(request)
        );
    }

    @GetMapping("/current-user/{userId}")
    public ResponseEntity<ApiResponse<CurrentUserResponseDTO>> getCurrentUser(
            @PathVariable Long userId) {

        return Response.success(
                "Current user details fetched successfully",
                authService.getCurrentUser(userId)
        );
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<ApiResponse<String>> logout(
            @PathVariable Long userId) {

        authService.logout(userId);

        return Response.success(
                "Logout successful",
                "SUCCESS"
        );
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @RequestBody ForgotPasswordRequestDTO request) {

        authService.forgotPassword(request.getUsername());

        return Response.success(
                "Password reset request submitted",
                "SUCCESS"
        );
    }
}