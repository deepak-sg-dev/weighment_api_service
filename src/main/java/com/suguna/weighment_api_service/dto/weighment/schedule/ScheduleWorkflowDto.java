package com.suguna.weighment_api_service.dto.weighment.schedule;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleWorkflowDto {

    private List<ScheduleWorkflowStepDto> lifting;
    private List<ScheduleWorkflowStepDto> delivery;
}
