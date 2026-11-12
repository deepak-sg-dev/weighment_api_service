package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleOrderDto {

    /** Retail ntail order number ({@code order_no}). */
    private String orderNo;
    private String customerCode;
    private String customerName;
    /** Shop GSTIN from {@code sug_gst_pkg.get_reg_no('GSTN','CS', cust_acct_site_id, SYSDATE)}. */
    private String gstNo;
    private double orderedQuantityKg;
    /** Requested birds from {@code sug_mai_retail_order_dtl.quantity}. */
    private double orderedBirdCount;
    /** Delivered kg from DTL {@code delv_weight}. */
    private Double deliveredQuantityKg;
    private Integer deliveredBirdCount;
    private String location;
    private Double latitude;
    private Double longitude;
    /** Per-order delivery: PENDING, IN_PROGRESS, COMPLETED (retail only). */
    private String deliveryStatus;
    /** ISO-8601 UTC when delivery was started for this order. */
    private String deliveryStartedAt;
    /** Per-shop delivery vehicle when set (retail). */
    private ScheduleVehicleDto deliveryVehicle;

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

    public String getGstNo() {
        return gstNo;
    }

    public void setGstNo(String gstNo) {
        this.gstNo = gstNo;
    }

    public double getOrderedQuantityKg() {
        return orderedQuantityKg;
    }

    public void setOrderedQuantityKg(double orderedQuantityKg) {
        this.orderedQuantityKg = orderedQuantityKg;
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

    public ScheduleVehicleDto getDeliveryVehicle() {
        return deliveryVehicle;
    }

    public void setDeliveryVehicle(ScheduleVehicleDto deliveryVehicle) {
        this.deliveryVehicle = deliveryVehicle;
    }
}
