package com.suguna.weighment_api_service.dto.weighment.schedule;

import java.util.List;

public class ScheduleListResponse {

    private boolean success;
    private List<ScheduleSummaryDto> schedules;

    public ScheduleListResponse() {}

    public static ScheduleListResponse success(List<ScheduleSummaryDto> schedules) {
        ScheduleListResponse response = new ScheduleListResponse();
        response.setSuccess(true);
        response.setSchedules(schedules);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<ScheduleSummaryDto> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleSummaryDto> schedules) {
        this.schedules = schedules;
    }
}
