package com.suguna.weighment_api_service.admin.security;

import com.suguna.weighment_api_service.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class AdminJwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    public AdminJwtAuthenticationFilter(
            JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            String token =
                    authHeader.substring(7);

            try {

                Claims claims =
                        jwtService.extractClaims(token);

                String userType =
                        claims.get("userType",
                                String.class);

                if ("ADMIN".equals(userType)) {

                    AuthenticatedAdmin admin =
                            new AuthenticatedAdmin(
                                    Long.valueOf(
                                            claims.getSubject()),
                                    claims.get(
                                            "username",
                                            String.class),
                                    claims.get(
                                            "role",
                                            String.class));

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    admin,
                                    null,
                                    List.of());

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(auth);
                }

            } catch (Exception ignored) {
            }
        }

        filterChain.doFilter(
                request,
                response);
    }
}