package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ScheduleListResponse {

    private boolean success;
    private List<ScheduleSummaryDto> schedules;
}
