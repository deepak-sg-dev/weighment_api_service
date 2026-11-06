package com.suguna.weighment_api_service.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration config = new CorsConfiguration();
        if (corsProperties.isEnabled() && corsProperties.hasOriginRules()) {
            List<String> origins = corsProperties.getAllowedOrigins();
            if (!origins.isEmpty()) {
                config.setAllowedOrigins(origins);
            }
            List<String> patterns = corsProperties.getAllowedOriginPatterns();
            if (!patterns.isEmpty()) {
                config.setAllowedOriginPatterns(patterns);
            }
            config.setAllowedMethods(corsProperties.getAllowedMethods());
            config.setAllowedHeaders(corsProperties.getAllowedHeaders());
            config.setExposedHeaders(corsProperties.getExposedHeaders());
            config.setAllowCredentials(corsProperties.isAllowCredentials());
            config.setMaxAge(corsProperties.getMaxAgeSeconds());
        } else {
            config.setAllowedOriginPatterns(List.of());
            config.setAllowedMethods(List.of());
            config.setAllowedHeaders(List.of());
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
