package com.suguna.weighment_api_service.dto.weighment.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Print payload for lifting DC or per-shop delivery DC. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeighmentDcResponse {

    public static final String TYPE_LIFTING = "LIFTING";
    public static final String TYPE_DELIVERY = "DELIVERY";

    private boolean success;
    private String documentType;
    private String scheduleId;
    private String deliveryId;
    private String scheduledAt;
    private String saleType;
    private String birdType;
    private String scaleType;
    private Double plannedQuantityKg;
    private Double orderedQuantityKg;
    private ScheduleBranchDto branch;
    private ScheduleCustomerDto customer;
    private ScheduleCustomerDto deliveryCustomer;
    private PartyContactDto farmer;
    private ScheduleFarmSummaryDto farm;
    private ScheduleSupervisorDto supervisor;
    private ScheduleVehicleDto vehicle;
    private String weighmentStartedAt;
    private String weighmentCompletedAt;
    private String scaleNo;
    private Integer birdStockNos;
    private Double feedStock;
    private List<WeighmentDcLineDto> lines;
    private WeighmentDcTotalsDto totals;
    private ScheduleDcDocumentDto print;
}
