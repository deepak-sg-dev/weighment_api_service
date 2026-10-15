package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.weighment.upload")
public class WeighmentUploadProperties {

    /**
     * Root directory for all weighment verification images.
     * Production: {@code /home/weighment}. Subfolders: {@code lifting/}, {@code delivery/}, then sale type and schedule no.
     */
    private String baseDir = "uploads";

    /** Maximum image upload size in bytes (default 5 MB). */
    private long maxImageBytes = 5L * 1024 * 1024;

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public long getMaxImageBytes() {
        return maxImageBytes;
    }

    public void setMaxImageBytes(long maxImageBytes) {
        this.maxImageBytes = maxImageBytes;
    }

    /** @deprecated Use {@link #getBaseDir()} */
    @Deprecated
    public String getVehiclePhotoDir() {
        return baseDir;
    }

    /** @deprecated Use {@link #setBaseDir(String)} */
    @Deprecated
    public void setVehiclePhotoDir(String vehiclePhotoDir) {
        this.baseDir = vehiclePhotoDir;
    }

    /** @deprecated Use {@link #getMaxImageBytes()} */
    @Deprecated
    public long getVehiclePhotoMaxBytes() {
        return maxImageBytes;
    }

    /** @deprecated Use {@link #setMaxImageBytes(long)} */
    @Deprecated
    public void setVehiclePhotoMaxBytes(long vehiclePhotoMaxBytes) {
        this.maxImageBytes = vehiclePhotoMaxBytes;
    }
}
