package com.suguna.weighment_api_service.admin.security;

import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;

@Getter
public class AuthenticatedAdmin implements AuthenticatedPrincipal {

    private final Long userId;
    private final String username;
    private final String role;

    public AuthenticatedAdmin(Long userId,
                              String username,
                              String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    @Override
    public String getName() {
        return username;
    }
}