package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.api.v2")
public class ApiV2Properties {

    /** When true, every weighment entry must include liveReadings + liveReadingsAverage. */
    private boolean liveReadingsRequired;

    private int otpExpiresSeconds = 300;
    private int otpResendMax = 3;
    private int otpResendCooldownSeconds = 30;

    public boolean isLiveReadingsRequired() {
        return liveReadingsRequired;
    }

    public void setLiveReadingsRequired(boolean liveReadingsRequired) {
        this.liveReadingsRequired = liveReadingsRequired;
    }

    public int getOtpExpiresSeconds() {
        return otpExpiresSeconds;
    }

    public void setOtpExpiresSeconds(int otpExpiresSeconds) {
        this.otpExpiresSeconds = otpExpiresSeconds;
    }

    public int getOtpResendMax() {
        return otpResendMax;
    }

    public void setOtpResendMax(int otpResendMax) {
        this.otpResendMax = otpResendMax;
    }

    public int getOtpResendCooldownSeconds() {
        return otpResendCooldownSeconds;
    }

    public void setOtpResendCooldownSeconds(int otpResendCooldownSeconds) {
        this.otpResendCooldownSeconds = otpResendCooldownSeconds;
    }
}
