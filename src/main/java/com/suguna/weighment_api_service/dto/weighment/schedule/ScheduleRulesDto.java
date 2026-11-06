package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleRulesDto {

    private int standardBirdsPerCage;
    private long stableDurationMs;
    private double toleranceMin;
    private double toleranceMax;
    private String captureMode;
    private PhotoRequirementDto photoRequirement;

    public ScheduleRulesDto() {}

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

    public double getToleranceMin() {
        return toleranceMin;
    }

    public void setToleranceMin(double toleranceMin) {
        this.toleranceMin = toleranceMin;
    }

    public double getToleranceMax() {
        return toleranceMax;
    }

    public void setToleranceMax(double toleranceMax) {
        this.toleranceMax = toleranceMax;
    }

    public String getCaptureMode() {
        return captureMode;
    }

    public void setCaptureMode(String captureMode) {
        this.captureMode = captureMode;
    }

    public PhotoRequirementDto getPhotoRequirement() {
        return photoRequirement;
    }

    public void setPhotoRequirement(PhotoRequirementDto photoRequirement) {
        this.photoRequirement = photoRequirement;
    }
}
