package com.suguna.weighment_api_service.dto.transaction;

/**
 * Submitted weighment for a schedule (header + lines), aligned with submission API v1.
 */
public class WeighmentSubmissionDetailResponse {

    private boolean success = true;
    private String message;
    private String scheduleId;
    private String transactionId;
    private String status;
    private String receivedAt;
    private SubmitCompletedWeighmentRequest submission;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public SubmitCompletedWeighmentRequest getSubmission() {
        return submission;
    }

    public void setSubmission(SubmitCompletedWeighmentRequest submission) {
        this.submission = submission;
    }
}
