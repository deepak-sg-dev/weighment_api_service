package com.suguna.weighment_api_service.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cross-origin access for browser / Flutter web / local dev tools. Native mobile HTTP clients do not
 * use CORS; this mainly helps web views and Swagger on another origin.
 */
@ConfigurationProperties(prefix = "app.api.cors")
public class CorsProperties {

    /** When false, Spring Security CORS is not applied (browser same-origin rules only). */
    private boolean enabled = false;

    /**
     * Exact origins, e.g. {@code https://weighment.example.com}. Prefer {@link #allowedOriginPatterns}
     * for localhost ports in dev.
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * Origin patterns (Spring 5.3+), e.g. {@code http://localhost:*}, {@code https://*.example.com}.
     */
    private List<String> allowedOriginPatterns = new ArrayList<>();

    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    private List<String> allowedHeaders = List.of("*");

    private List<String> exposedHeaders = List.of("Authorization");

    private boolean allowCredentials = true;

    /** Preflight cache seconds. */
    private long maxAgeSeconds = 3600;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins == null ? new ArrayList<>() : allowedOrigins;
    }

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns == null ? new ArrayList<>() : allowedOriginPatterns;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public long getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public void setMaxAgeSeconds(long maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    public boolean hasOriginRules() {
        return !allowedOrigins.isEmpty() || !allowedOriginPatterns.isEmpty();
    }
}
