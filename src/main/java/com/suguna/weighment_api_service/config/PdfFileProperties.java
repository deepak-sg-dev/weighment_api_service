package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code pdf.file.path} — reserved for generated PDF storage (not used by weighment uploads yet). */
@ConfigurationProperties(prefix = "pdf.file")
public class PdfFileProperties {

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
