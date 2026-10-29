package com.suguna.weighment_api_service.dto.weighment.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class MarkScheduleDownloadedRequest {

    @NotBlank
    @Size(max = 128)
    private String deviceId;

    @NotNull
    private Instant downloadedAt;

    @NotBlank
    @Size(max = 32)
    private String appVersion;

    public MarkScheduleDownloadedRequest() {}

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Instant getDownloadedAt() {
        return downloadedAt;
    }

    public void setDownloadedAt(Instant downloadedAt) {
        this.downloadedAt = downloadedAt;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}
