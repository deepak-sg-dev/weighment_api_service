package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleDetailResponse {

    private boolean success;
    private ScheduleDetailDto schedule;
}
