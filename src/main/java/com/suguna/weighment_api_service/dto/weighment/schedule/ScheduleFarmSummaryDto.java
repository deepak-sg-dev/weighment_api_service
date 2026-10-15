package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleFarmSummaryDto {

    private String id;
    private String name;
    private String location;
}
