package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleActionResponse {

    private boolean success;
    private String scheduleId;
    private String status;
    private String message;
}
