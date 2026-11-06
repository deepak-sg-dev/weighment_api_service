package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleActionResponse {

    private boolean success;
    private String scheduleId;
    private String status;
    private String message;

    public ScheduleActionResponse() {}

    public static ScheduleActionResponse success(String scheduleId, String status, String message) {
        ScheduleActionResponse response = new ScheduleActionResponse();
        response.setSuccess(true);
        response.setScheduleId(scheduleId);
        response.setStatus(status);
        response.setMessage(message);
        return response;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
