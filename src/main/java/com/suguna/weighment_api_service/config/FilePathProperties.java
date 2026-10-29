package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Server filesystem paths (environment-specific). Weighment verification images use
 * {@link #resolveWeighmentVerificationRoot(String)} — typically {@code file.upload-dir} or
 * {@code file.weighment-verification-dir}.
 */
@ConfigurationProperties(prefix = "file")
public class FilePathProperties {

    private final Upload upload = new Upload();

    /** Optional override; when set, used instead of {@link #uploadDir} for weighment photos. */
    private String weighmentVerificationDir;

    /** Reserved for future banner assets (not used by weighment API yet). */
    private String bannerDir;

    public Upload getUpload() {
        return upload;
    }

    public String getUploadBasePath() {
        return upload.getBasePath();
    }

    public void setUploadBasePath(String uploadBasePath) {
        upload.setBasePath(uploadBasePath);
    }

    /** Primary upload root ({@code file.upload-dir}). */
    public String getUploadDir() {
        return upload.getDir();
    }

    public void setUploadDir(String uploadDir) {
        upload.setDir(uploadDir);
    }

    public String getWeighmentVerificationDir() {
        return weighmentVerificationDir;
    }

    public void setWeighmentVerificationDir(String weighmentVerificationDir) {
        this.weighmentVerificationDir = weighmentVerificationDir;
    }

    public String getBannerDir() {
        return bannerDir;
    }

    public void setBannerDir(String bannerDir) {
        this.bannerDir = bannerDir;
    }

    /**
     * Root directory for lifting/delivery verification images.
     *
     * @param legacyAppWeighmentBaseDir optional {@code app.weighment.upload.base-dir} when file.* unset
     */
    public String resolveWeighmentVerificationRoot(String legacyAppWeighmentBaseDir) {
        if (isSet(weighmentVerificationDir)) {
            return weighmentVerificationDir.trim();
        }
        if (isSet(upload.getDir())) {
            return upload.getDir().trim();
        }
        if (isSet(upload.getBasePath())) {
            return upload.getBasePath().trim();
        }
        if (isSet(legacyAppWeighmentBaseDir)) {
            return legacyAppWeighmentBaseDir.trim();
        }
        return "uploads";
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }

    public static class Upload {
        /** {@code file.upload.base-path} */
        private String basePath;

        /** {@code file.upload-dir} */
        private String dir;

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }
}
