package com.suguna.weighment_api_service.dto.weighment.schedule;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleSummaryDto {

    private String scheduleId;
    private String status;
    private boolean downloaded;
    private String saleType;
    private String birdType;
    private String scheduledAt;
    private double plannedQuantity;
    private String scaleType;
    private String liftingStatus;
    private String deliveryStatus;
    private ScheduleFarmSummaryDto farm;
    private int orderCount;
    private List<ScheduleOrderDto> orders;
}
