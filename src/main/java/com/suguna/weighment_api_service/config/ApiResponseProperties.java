package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.api.response")
public class ApiResponseProperties {

    /**
     * Include request path on error responses (useful for debugging).
     */
    private boolean includePath = true;

    /**
     * Include ISO-8601 timestamp on every response.
     */
    private boolean includeTimestamp = true;

    /**
     * When true, generic 500 responses hide internal exception messages.
     */
    private boolean maskInternalErrors = true;

    public boolean isIncludePath() {
        return includePath;
    }

    public void setIncludePath(boolean includePath) {
        this.includePath = includePath;
    }

    public boolean isIncludeTimestamp() {
        return includeTimestamp;
    }

    public void setIncludeTimestamp(boolean includeTimestamp) {
        this.includeTimestamp = includeTimestamp;
    }

    public boolean isMaskInternalErrors() {
        return maskInternalErrors;
    }

    public void setMaskInternalErrors(boolean maskInternalErrors) {
        this.maskInternalErrors = maskInternalErrors;
    }
}
