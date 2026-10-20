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
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
