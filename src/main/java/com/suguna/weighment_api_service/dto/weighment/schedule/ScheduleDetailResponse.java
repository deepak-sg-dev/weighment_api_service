package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleDetailResponse {

    private boolean success;
    private ScheduleDetailDto schedule;

    public ScheduleDetailResponse() {}

    public static ScheduleDetailResponse success(ScheduleDetailDto schedule) {
        ScheduleDetailResponse response = new ScheduleDetailResponse();
        response.setSuccess(true);
        response.setSchedule(schedule);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ScheduleDetailDto getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleDetailDto schedule) {
        this.schedule = schedule;
    }
}
