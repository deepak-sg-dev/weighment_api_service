package com.suguna.weighment_api_service.dto.transaction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public class SubmitCompletedWeighmentRequest {

    @NotBlank
    private String scheduleId;

    @NotBlank
    private String deviceId;

    private String supervisorId;

    private String saleType;

    private String birdType;

    /** @deprecated Use {@link #birdType}; accepted for older clients. */
    @Deprecated
    @JsonAlias("operationType")
    private String operationType;

    @Valid
    private ScaleInfo scale;

    @Valid
    @NotNull
    private FarmValidation farmValidation;

    @Valid
    private VehicleInfo vehicle;

    private Double plannedQuantity;

    @NotEmpty
    @JsonProperty("weighmentEntries")
    @JsonAlias("rows")
    private List<@Valid WeighmentEntry> weighmentEntries;

    @Valid
    @NotNull
    private WeighmentTotals totals;

    @Valid
    private CompletionInfo completion;

    @Valid
    private AuditInfo audit;

    public SubmitCompletedWeighmentRequest() {}

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public ScaleInfo getScale() {
        return scale;
    }

    public void setScale(ScaleInfo scale) {
        this.scale = scale;
    }

    public FarmValidation getFarmValidation() {
        return farmValidation;
    }

    public void setFarmValidation(FarmValidation farmValidation) {
        this.farmValidation = farmValidation;
    }

    public VehicleInfo getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleInfo vehicle) {
        this.vehicle = vehicle;
    }

    public Double getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(Double plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public List<WeighmentEntry> getWeighmentEntries() {
        return weighmentEntries;
    }

    public void setWeighmentEntries(List<WeighmentEntry> weighmentEntries) {
        this.weighmentEntries = weighmentEntries;
    }

    public WeighmentTotals getTotals() {
        return totals;
    }

    public void setTotals(WeighmentTotals totals) {
        this.totals = totals;
    }

    public CompletionInfo getCompletion() {
        return completion;
    }

    public void setCompletion(CompletionInfo completion) {
        this.completion = completion;
    }

    public AuditInfo getAudit() {
        return audit;
    }

    public void setAudit(AuditInfo audit) {
        this.audit = audit;
    }

    public String effectiveBirdType() {
        if (birdType != null && !birdType.isBlank()) {
            return birdType.trim();
        }
        if (operationType != null && !operationType.isBlank()) {
            return operationType.trim();
        }
        return null;
    }

    public String effectiveSaleType() {
        return saleType == null ? null : saleType.trim();
    }

    public static class ScaleInfo {
        private String scaleType;
        private String deviceId;
        private String pcbId;
        private String firmwareVersion;

        public String getScaleType() {
            return scaleType;
        }

        public void setScaleType(String scaleType) {
            this.scaleType = scaleType;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getPcbId() {
            return pcbId;
        }

        public void setPcbId(String pcbId) {
            this.pcbId = pcbId;
        }

        public String getFirmwareVersion() {
            return firmwareVersion;
        }

        public void setFirmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
        }
    }

    public static class FarmValidation {
        private Double farmLatitude;
        private Double farmLongitude;
        private Integer configuredGeofenceRadiusMeters;
        private Double capturedLatitude;
        private Double capturedLongitude;
        private Double accuracyMeters;
        private Boolean geofencePassed;
        private Boolean validatedLocally;
        private Instant validatedAt;
        private Boolean farmerOtpVerified;

        public Double getFarmLatitude() {
            return farmLatitude;
        }

        public void setFarmLatitude(Double farmLatitude) {
            this.farmLatitude = farmLatitude;
        }

        public Double getFarmLongitude() {
            return farmLongitude;
        }

        public void setFarmLongitude(Double farmLongitude) {
            this.farmLongitude = farmLongitude;
        }

        public Integer getConfiguredGeofenceRadiusMeters() {
            return configuredGeofenceRadiusMeters;
        }

        public void setConfiguredGeofenceRadiusMeters(Integer configuredGeofenceRadiusMeters) {
            this.configuredGeofenceRadiusMeters = configuredGeofenceRadiusMeters;
        }

        public Double getCapturedLatitude() {
            return capturedLatitude;
        }

        public void setCapturedLatitude(Double capturedLatitude) {
            this.capturedLatitude = capturedLatitude;
        }

        public Double getCapturedLongitude() {
            return capturedLongitude;
        }

        public void setCapturedLongitude(Double capturedLongitude) {
            this.capturedLongitude = capturedLongitude;
        }

        public Double getAccuracyMeters() {
            return accuracyMeters;
        }

        public void setAccuracyMeters(Double accuracyMeters) {
            this.accuracyMeters = accuracyMeters;
        }

        public Boolean getGeofencePassed() {
            return geofencePassed;
        }

        public void setGeofencePassed(Boolean geofencePassed) {
            this.geofencePassed = geofencePassed;
        }

        public Boolean getValidatedLocally() {
            return validatedLocally;
        }

        public void setValidatedLocally(Boolean validatedLocally) {
            this.validatedLocally = validatedLocally;
        }

        public Instant getValidatedAt() {
            return validatedAt;
        }

        public void setValidatedAt(Instant validatedAt) {
            this.validatedAt = validatedAt;
        }

        public Boolean getFarmerOtpVerified() {
            return farmerOtpVerified;
        }

        public void setFarmerOtpVerified(Boolean farmerOtpVerified) {
            this.farmerOtpVerified = farmerOtpVerified;
        }
    }

    public static class VehicleInfo {
        private String registrationNumber;

        public String getRegistrationNumber() {
            return registrationNumber;
        }

        public void setRegistrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
        }
    }

    public static class WeighmentEntry {
        private Integer sequence;
        private String category;
        private Integer birdsApplied;
        private Double gross;
        private Double net;
        private Double averageBirdWeight;
        private Double weightDifference;
        private String status;
        private Instant startTime;
        private Instant endTime;

        public Integer getSequence() {
            return sequence;
        }

        public void setSequence(Integer sequence) {
            this.sequence = sequence;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Integer getBirdsApplied() {
            return birdsApplied;
        }

        public void setBirdsApplied(Integer birdsApplied) {
            this.birdsApplied = birdsApplied;
        }

        public Double getGross() {
            return gross;
        }

        public void setGross(Double gross) {
            this.gross = gross;
        }

        public Double getNet() {
            return net;
        }

        public void setNet(Double net) {
            this.net = net;
        }

        public Double getAverageBirdWeight() {
            return averageBirdWeight;
        }

        public void setAverageBirdWeight(Double averageBirdWeight) {
            this.averageBirdWeight = averageBirdWeight;
        }

        public Double getWeightDifference() {
            return weightDifference;
        }

        public void setWeightDifference(Double weightDifference) {
            this.weightDifference = weightDifference;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Instant getStartTime() {
            return startTime;
        }

        public void setStartTime(Instant startTime) {
            this.startTime = startTime;
        }

        public Instant getEndTime() {
            return endTime;
        }

        public void setEndTime(Instant endTime) {
            this.endTime = endTime;
        }
    }

    public static class WeighmentTotals {
        private Integer totalCagesOrBatches;
        private Integer totalBirds;
        private Double totalTare;
        private Double totalGross;
        private Double totalNet;
        private Double overallAverageBirdWeight;

        public Integer getTotalCagesOrBatches() {
            return totalCagesOrBatches;
        }

        public void setTotalCagesOrBatches(Integer totalCagesOrBatches) {
            this.totalCagesOrBatches = totalCagesOrBatches;
        }

        public Integer getTotalBirds() {
            return totalBirds;
        }

        public void setTotalBirds(Integer totalBirds) {
            this.totalBirds = totalBirds;
        }

        public Double getTotalTare() {
            return totalTare;
        }

        public void setTotalTare(Double totalTare) {
            this.totalTare = totalTare;
        }

        public Double getTotalGross() {
            return totalGross;
        }

        public void setTotalGross(Double totalGross) {
            this.totalGross = totalGross;
        }

        public Double getTotalNet() {
            return totalNet;
        }

        public void setTotalNet(Double totalNet) {
            this.totalNet = totalNet;
        }

        public Double getOverallAverageBirdWeight() {
            return overallAverageBirdWeight;
        }

        public void setOverallAverageBirdWeight(Double overallAverageBirdWeight) {
            this.overallAverageBirdWeight = overallAverageBirdWeight;
        }
    }

    public static class CompletionInfo {
        private Integer claimedBirds;
        private Integer replacementBirds;
        private Integer lameBirds;
        private Instant weighingStartTime;
        private Instant outTime;
        private String remarks;
        private List<PartyAcknowledgement> acknowledgements;

        public Integer getClaimedBirds() {
            return claimedBirds;
        }

        public void setClaimedBirds(Integer claimedBirds) {
            this.claimedBirds = claimedBirds;
        }

        public Integer getReplacementBirds() {
            return replacementBirds;
        }

        public void setReplacementBirds(Integer replacementBirds) {
            this.replacementBirds = replacementBirds;
        }

        public Integer getLameBirds() {
            return lameBirds;
        }

        public void setLameBirds(Integer lameBirds) {
            this.lameBirds = lameBirds;
        }

        public Instant getWeighingStartTime() {
            return weighingStartTime;
        }

        public void setWeighingStartTime(Instant weighingStartTime) {
            this.weighingStartTime = weighingStartTime;
        }

        public Instant getOutTime() {
            return outTime;
        }

        public void setOutTime(Instant outTime) {
            this.outTime = outTime;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public List<PartyAcknowledgement> getAcknowledgements() {
            return acknowledgements;
        }

        public void setAcknowledgements(List<PartyAcknowledgement> acknowledgements) {
            this.acknowledgements = acknowledgements;
        }
    }

    public static class PartyAcknowledgement {
        private String party;
        private String reference;

        public String getParty() {
            return party;
        }

        public void setParty(String party) {
            this.party = party;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }
    }

    public static class AuditInfo {
        private List<Object> abortedRows;
        private List<Object> stabilityErrors;
        private List<Object> toleranceAlerts;
        private List<Object> overrides;

        public List<Object> getAbortedRows() {
            return abortedRows;
        }

        public void setAbortedRows(List<Object> abortedRows) {
            this.abortedRows = abortedRows;
        }

        public List<Object> getStabilityErrors() {
            return stabilityErrors;
        }

        public void setStabilityErrors(List<Object> stabilityErrors) {
            this.stabilityErrors = stabilityErrors;
        }

        public List<Object> getToleranceAlerts() {
            return toleranceAlerts;
        }

        public void setToleranceAlerts(List<Object> toleranceAlerts) {
            this.toleranceAlerts = toleranceAlerts;
        }

        public List<Object> getOverrides() {
            return overrides;
        }

        public void setOverrides(List<Object> overrides) {
            this.overrides = overrides;
        }
    }
}
