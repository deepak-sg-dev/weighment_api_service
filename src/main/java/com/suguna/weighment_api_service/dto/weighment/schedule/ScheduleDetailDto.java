package com.suguna.weighment_api_service.dto.weighment.schedule;

import java.util.List;

public class ScheduleDetailDto {

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
    private ScheduleBranchDto branch;
    private String salesOrderNo;
    private Double declaredBodyWeightKg;
    private ScheduleVehicleDto vehicle;
    private ScheduleSupervisorDto supervisor;
    private FarmDto farm;
    private PartyContactDto farmer;
    /** Indent customer from lifting order ({@code CUSTOMERCODE}/{@code CUS_NAME}/{@code CUS_LOCATION}). */
    private ScheduleCustomerDto customer;
    private ScheduleRulesDto rules;
    private List<ScheduleDeliveryDto> deliveries;
    private ScheduleOtpDetailDto otp;
    private ScheduleWorkflowDto workflow;
    /** Optional DC print fields (slip, HSN, GST, bank, etc.). Keys are always present; values may be null. */
    private ScheduleDcDocumentDto dc;

    public ScheduleDetailDto() {}

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

    public ScheduleBranchDto getBranch() {
        return branch;
    }

    public void setBranch(ScheduleBranchDto branch) {
        this.branch = branch;
    }

    public String getSalesOrderNo() {
        return salesOrderNo;
    }

    public void setSalesOrderNo(String salesOrderNo) {
        this.salesOrderNo = salesOrderNo;
    }

    public Double getDeclaredBodyWeightKg() {
        return declaredBodyWeightKg;
    }

    public void setDeclaredBodyWeightKg(Double declaredBodyWeightKg) {
        this.declaredBodyWeightKg = declaredBodyWeightKg;
    }

    public ScheduleVehicleDto getVehicle() {
        return vehicle;
    }

    public void setVehicle(ScheduleVehicleDto vehicle) {
        this.vehicle = vehicle;
    }

    public ScheduleSupervisorDto getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(ScheduleSupervisorDto supervisor) {
        this.supervisor = supervisor;
    }

    public FarmDto getFarm() {
        return farm;
    }

    public void setFarm(FarmDto farm) {
        this.farm = farm;
    }

    public PartyContactDto getFarmer() {
        return farmer;
    }

    public void setFarmer(PartyContactDto farmer) {
        this.farmer = farmer;
    }

    public ScheduleCustomerDto getCustomer() {
        return customer;
    }

    public void setCustomer(ScheduleCustomerDto customer) {
        this.customer = customer;
    }

    public ScheduleRulesDto getRules() {
        return rules;
    }

    public void setRules(ScheduleRulesDto rules) {
        this.rules = rules;
    }

    public List<ScheduleDeliveryDto> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<ScheduleDeliveryDto> deliveries) {
        this.deliveries = deliveries;
    }

    public ScheduleOtpDetailDto getOtp() {
        return otp;
    }

    public void setOtp(ScheduleOtpDetailDto otp) {
        this.otp = otp;
    }

    public ScheduleWorkflowDto getWorkflow() {
        return workflow;
    }

    public void setWorkflow(ScheduleWorkflowDto workflow) {
        this.workflow = workflow;
    }

    public ScheduleDcDocumentDto getDc() {
        return dc;
    }

    public void setDc(ScheduleDcDocumentDto dc) {
        this.dc = dc;
    }
}
