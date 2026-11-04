package com.suguna.weighment_api_service.config;

import com.suguna.weighment_api_service.admin.security.AdminJwtAuthenticationFilter;
import com.suguna.weighment_api_service.security.JwtAuthenticationFilter;
import com.suguna.weighment_api_service.security.JwtService;
import com.suguna.weighment_api_service.security.RestAuthenticationEntryPoint;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/device-login",
            "/system/ping",

            // Admin Auth APIs
            "/api/admin/auth/login",
            "/api/admin/auth/forgot-password",

            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",

            "/actuator/health",
            "/actuator/info"
    };

    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(
            RestAuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @PostConstruct
    public void init() {
        System.out.println("========================================");
        System.out.println("=== UPDATED SECURITY CONFIG LOADED ===");
        System.out.println("========================================");
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    AdminJwtAuthenticationFilter adminJwtAuthenticationFilter(
            JwtService jwtService) {
        return new AdminJwtAuthenticationFilter(jwtService);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AdminJwtAuthenticationFilter adminJwtAuthenticationFilter)
            throws Exception {

        http.cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .exceptionHandling(handler ->
                        handler.authenticationEntryPoint(
                                authenticationEntryPoint))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**")
                        .permitAll()

                        .requestMatchers(PUBLIC_ENDPOINTS)
                        .permitAll()

                        .anyRequest()
                        .authenticated())

                .addFilterAfter(
                        jwtAuthenticationFilter,
                        SecurityContextHolderFilter.class)

                .addFilterAfter(
                        adminJwtAuthenticationFilter,
                        JwtAuthenticationFilter.class);

        return http.build();
    }
}