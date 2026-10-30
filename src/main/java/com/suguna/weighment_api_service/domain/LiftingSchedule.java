package com.suguna.weighment_api_service.domain;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import java.time.Instant;

/** Lifting schedule row mapped from {@code sug_mai_birds_lifting_order} (explicit accessors for IDE/Maven parity). */
public class LiftingSchedule {

    private String scheduleId;
    private String supervisorId;
    private String orderSource;
    private Instant scheduledAt;
    private double plannedQuantityKg;
    private double declaredBodyWeightKg;
    private String branchCode;
    private String branchId;
    private Long salesOrderNo;
    private String vehicleNumber;
    private String farmLocation;
    private String farmId;
    private String farmName;
    private double farmLatitude;
    private double farmLongitude;
    private int geofenceRadiusMeters;
    private String farmerId;
    private String farmerName;
    private String farmerMobile;
    private String traderId;
    private String traderName;
    private String traderMobile;
    private String productId;
    private String productName;
    private int plannedQuantity;
    private String operationType;
    private String scaleType;
    private ScheduleStatus status;
    private boolean downloaded;
    private String liftingStatus;
    private String deliveryStatus;
    private Instant startedAt;
    private int standardBirdsPerCage;
    private long stableDurationMs;
    private double toleranceMin;
    private double toleranceMax;
    private String captureMode;
    private boolean completionPhotoRequired;

    public LiftingSchedule() {}

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public double getPlannedQuantityKg() {
        return plannedQuantityKg;
    }

    public void setPlannedQuantityKg(double plannedQuantityKg) {
        this.plannedQuantityKg = plannedQuantityKg;
    }

    public double getDeclaredBodyWeightKg() {
        return declaredBodyWeightKg;
    }

    public void setDeclaredBodyWeightKg(double declaredBodyWeightKg) {
        this.declaredBodyWeightKg = declaredBodyWeightKg;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public Long getSalesOrderNo() {
        return salesOrderNo;
    }

    public void setSalesOrderNo(Long salesOrderNo) {
        this.salesOrderNo = salesOrderNo;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getFarmLocation() {
        return farmLocation;
    }

    public void setFarmLocation(String farmLocation) {
        this.farmLocation = farmLocation;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public double getFarmLatitude() {
        return farmLatitude;
    }

    public void setFarmLatitude(double farmLatitude) {
        this.farmLatitude = farmLatitude;
    }

    public double getFarmLongitude() {
        return farmLongitude;
    }

    public void setFarmLongitude(double farmLongitude) {
        this.farmLongitude = farmLongitude;
    }

    public int getGeofenceRadiusMeters() {
        return geofenceRadiusMeters;
    }

    public void setGeofenceRadiusMeters(int geofenceRadiusMeters) {
        this.geofenceRadiusMeters = geofenceRadiusMeters;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getFarmerMobile() {
        return farmerMobile;
    }

    public void setFarmerMobile(String farmerMobile) {
        this.farmerMobile = farmerMobile;
    }

    public String getTraderId() {
        return traderId;
    }

    public void setTraderId(String traderId) {
        this.traderId = traderId;
    }

    public String getTraderName() {
        return traderName;
    }

    public void setTraderName(String traderName) {
        this.traderName = traderName;
    }

    public String getTraderMobile() {
        return traderMobile;
    }

    public void setTraderMobile(String traderMobile) {
        this.traderMobile = traderMobile;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(int plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getScaleType() {
        return scaleType;
    }

    public void setScaleType(String scaleType) {
        this.scaleType = scaleType;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public String getLiftingStatus() {
        return liftingStatus;
    }

    public void setLiftingStatus(String liftingStatus) {
        this.liftingStatus = liftingStatus;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
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

    public boolean isCompletionPhotoRequired() {
        return completionPhotoRequired;
    }

    public void setCompletionPhotoRequired(boolean completionPhotoRequired) {
        this.completionPhotoRequired = completionPhotoRequired;
    }
}
