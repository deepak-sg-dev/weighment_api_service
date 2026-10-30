package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.LoginRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.CurrentUserResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.LoginResponseDTO;

public interface AuthService {

    /**
     * Login
     */
    LoginResponseDTO login(LoginRequestDTO request);

    /**
     * Current User Details
     */
    CurrentUserResponseDTO getCurrentUser(Long userId);

    /**
     * Logout
     */
    void logout(Long userId);

    void forgotPassword(String username);
}