package com.suguna.weighment_api_service.dto.transaction;

import java.util.ArrayList;
import java.util.List;

/** Line-level weighment history for a schedule ({@code SUG_MAI_BIRDS_WEIGHMENT_DTL}). */
public class WeighmentSubmissionHistoryResponse {

    private boolean success = true;
    private String scheduleId;
    private String transactionId;
    private List<WeighmentHistoryLineDto> lines = new ArrayList<>();

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<WeighmentHistoryLineDto> getLines() {
        return lines;
    }

    public void setLines(List<WeighmentHistoryLineDto> lines) {
        this.lines = lines == null ? new ArrayList<>() : lines;
    }

    public static class WeighmentHistoryLineDto {

        private String dtlId;
        private Integer sequence;
        private String category;
        private Integer birdsApplied;
        private Double tareWeight;
        private Double gross;
        private Double net;
        private Double averageBirdWeight;
        private Double weightDifference;
        private String status;
        private String startTime;
        private String endTime;
        private Double captureLatitude;
        private Double captureLongitude;
        private Double captureAccuracyMeters;
        private String scaleDeviceId;
        private String deviceId;
        private String rowStatus;
        private String recordedAt;

        public String getDtlId() {
            return dtlId;
        }

        public void setDtlId(String dtlId) {
            this.dtlId = dtlId;
        }

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

        public Double getTareWeight() {
            return tareWeight;
        }

        public void setTareWeight(Double tareWeight) {
            this.tareWeight = tareWeight;
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

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public Double getCaptureLatitude() {
            return captureLatitude;
        }

        public void setCaptureLatitude(Double captureLatitude) {
            this.captureLatitude = captureLatitude;
        }

        public Double getCaptureLongitude() {
            return captureLongitude;
        }

        public void setCaptureLongitude(Double captureLongitude) {
            this.captureLongitude = captureLongitude;
        }

        public Double getCaptureAccuracyMeters() {
            return captureAccuracyMeters;
        }

        public void setCaptureAccuracyMeters(Double captureAccuracyMeters) {
            this.captureAccuracyMeters = captureAccuracyMeters;
        }

        public String getScaleDeviceId() {
            return scaleDeviceId;
        }

        public void setScaleDeviceId(String scaleDeviceId) {
            this.scaleDeviceId = scaleDeviceId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getRowStatus() {
            return rowStatus;
        }

        public void setRowStatus(String rowStatus) {
            this.rowStatus = rowStatus;
        }

        public String getRecordedAt() {
            return recordedAt;
        }

        public void setRecordedAt(String recordedAt) {
            this.recordedAt = recordedAt;
        }
    }
}
