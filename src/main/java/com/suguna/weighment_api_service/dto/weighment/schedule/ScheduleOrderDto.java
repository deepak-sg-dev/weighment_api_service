package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleOrderDto {

    /** Retail ntail order number ({@code order_no}). */
    private String orderNo;
    private String customerCode;
    private String customerName;
    private double orderedQuantityKg;
    private String location;
    private Double latitude;
    private Double longitude;

    public ScheduleOrderDto() {}

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
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

    public double getOrderedQuantityKg() {
        return orderedQuantityKg;
    }

    public void setOrderedQuantityKg(double orderedQuantityKg) {
        this.orderedQuantityKg = orderedQuantityKg;
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
}
