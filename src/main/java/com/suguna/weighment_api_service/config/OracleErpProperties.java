package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oracle.erp")
public class OracleErpProperties {

    private String applicationCode = "BRO-WEIGHMENT";
    private int defaultGeofenceRadiusMeters = 200;
    private int standardBirdsPerCage = 12;
    private long stableDurationMs = 2000L;
    /** Limit list API to recent orders (0 = no date filter). */
    private int scheduleListLookbackDays = 90;

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public int getDefaultGeofenceRadiusMeters() {
        return defaultGeofenceRadiusMeters;
    }

    public void setDefaultGeofenceRadiusMeters(int defaultGeofenceRadiusMeters) {
        this.defaultGeofenceRadiusMeters = defaultGeofenceRadiusMeters;
    }

    public int getStandardBirdsPerCage() {
        return standardBirdsPerCage;
    }

    public void setStandardBirdsPerCage(int standardBirdsPerCage) {
        this.standardBirdsPerCage = standardBirdsPerCage;
    }

    public long getStableDurationMs() {
        return stableDurationMs;
    }

    public void setStableDurationMs(long stableDurationMs) {
        this.stableDurationMs = stableDurationMs;
    }

    public int getScheduleListLookbackDays() {
        return scheduleListLookbackDays;
    }

    public void setScheduleListLookbackDays(int scheduleListLookbackDays) {
        this.scheduleListLookbackDays = scheduleListLookbackDays;
    }
}
