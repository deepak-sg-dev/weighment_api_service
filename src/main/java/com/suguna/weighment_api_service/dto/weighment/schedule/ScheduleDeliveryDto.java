package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleDeliveryDto {

    private String deliveryId;
    private int sequence;
    private String dropPointName;
    private String customerCode;
    private String customerName;
    private String location;
    private Double latitude;
    private Double longitude;
    private int geofenceRadiusMeters;
    private double quantityKg;
    /** Requested birds from {@code sug_mai_retail_order_dtl.quantity}. */
    private double orderedBirdCount;
    private Double deliveredQuantityKg;
    private Integer deliveredBirdCount;
    /** Same as {@link #deliveryStatus}. */
    private String status;
    /** Per-order delivery: PENDING, IN_PROGRESS, COMPLETED. */
    private String deliveryStatus;
    private String deliveryStartedAt;
    /** Per-shop OTP (V2 retail); maps to retail {@code order_no} / {@link #deliveryId}. */
    private PartyOtpDetailDto customerOtp;

    public ScheduleDeliveryDto() {}

    public String getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getDropPointName() {
        return dropPointName;
    }

    public void setDropPointName(String dropPointName) {
        this.dropPointName = dropPointName;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getGeofenceRadiusMeters() {
        return geofenceRadiusMeters;
    }

    public void setGeofenceRadiusMeters(int geofenceRadiusMeters) {
        this.geofenceRadiusMeters = geofenceRadiusMeters;
    }

    public double getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(double quantityKg) {
        this.quantityKg = quantityKg;
    }

    public double getOrderedBirdCount() {
        return orderedBirdCount;
    }

    public void setOrderedBirdCount(double orderedBirdCount) {
        this.orderedBirdCount = orderedBirdCount;
    }

    public Double getDeliveredQuantityKg() {
        return deliveredQuantityKg;
    }

    public void setDeliveredQuantityKg(Double deliveredQuantityKg) {
        this.deliveredQuantityKg = deliveredQuantityKg;
    }

    public Integer getDeliveredBirdCount() {
        return deliveredBirdCount;
    }

    public void setDeliveredBirdCount(Integer deliveredBirdCount) {
        this.deliveredBirdCount = deliveredBirdCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getDeliveryStartedAt() {
        return deliveryStartedAt;
    }

    public void setDeliveryStartedAt(String deliveryStartedAt) {
        this.deliveryStartedAt = deliveryStartedAt;
    }

    public PartyOtpDetailDto getCustomerOtp() {
        return customerOtp;
    }

    public void setCustomerOtp(PartyOtpDetailDto customerOtp) {
        this.customerOtp = customerOtp;
    }
}
