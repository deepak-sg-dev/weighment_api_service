package com.suguna.weighment_api_service.dto.weighment.schedule;

import java.util.List;

public class ScheduleSummaryDto {

    private String scheduleId;
    private String status;
    private boolean downloaded;
    private String saleType;
    private String birdType;
    private String scheduledAt;
    private String startedAt;
    private double plannedQuantity;
    private String scaleType;
    private String liftingStatus;
    private String deliveryStatus;
    private ScheduleFarmSummaryDto farm;
    private int orderCount;
    private List<ScheduleOrderDto> orders;

    public ScheduleSummaryDto() {}

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public String getSaleType() {
        return saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }

    public String getBirdType() {
        return birdType;
    }

    public void setBirdType(String birdType) {
        this.birdType = birdType;
    }

    public String getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(String scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public double getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(double plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public String getScaleType() {
        return scaleType;
    }

    public void setScaleType(String scaleType) {
        this.scaleType = scaleType;
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

    public ScheduleFarmSummaryDto getFarm() {
        return farm;
    }

    public void setFarm(ScheduleFarmSummaryDto farm) {
        this.farm = farm;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public List<ScheduleOrderDto> getOrders() {
        return orders;
    }

    public void setOrders(List<ScheduleOrderDto> orders) {
        this.orders = orders;
    }
}
