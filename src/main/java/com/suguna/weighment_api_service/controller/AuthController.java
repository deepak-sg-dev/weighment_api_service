package com.suguna.weighment_api_service.controller;

import com.suguna.weighment_api_service.dto.auth.DeviceLoginRequest;
import com.suguna.weighment_api_service.dto.auth.DeviceLoginResponse;
import com.suguna.weighment_api_service.dto.auth.LogoutRequest;
import com.suguna.weighment_api_service.dto.auth.SupervisorMeResponse;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.service.AuthService;
import com.suguna.weighment_api_service.util.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @SecurityRequirements
    @PostMapping("/device-login")
    public ResponseEntity<DeviceLoginResponse> deviceLogin(@Valid @RequestBody DeviceLoginRequest request) {
        return ResponseEntity.ok(authService.deviceLogin(request));
    }

    @GetMapping("/me")
    public ResponseEntity<SupervisorMeResponse> me(@AuthenticationPrincipal AuthenticatedSupervisor supervisor) {
        return ResponseEntity.ok(authService.currentSupervisor(supervisor));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @Valid @RequestBody LogoutRequest request) {
        authService.logout(supervisor, request.getDeviceId());
        return ApiResponses.okEmpty();
    }
}
