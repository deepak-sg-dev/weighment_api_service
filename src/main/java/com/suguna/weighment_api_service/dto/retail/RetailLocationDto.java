package com.suguna.weighment_api_service.dto.retail;

public class RetailLocationDto {

    private String retailerId;
    private String retailerName;
    private String retailOrderId;
    private double latitude;
    private double longitude;
    private String address;

    public RetailLocationDto() {}

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getRetailOrderId() {
        return retailOrderId;
    }

    public void setRetailOrderId(String retailOrderId) {
        this.retailOrderId = retailOrderId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
