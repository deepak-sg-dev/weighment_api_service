package com.suguna.weighment_api_service.dto.retail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailDeliveryOrderListResponse {

    private boolean success;
    private String scheduleId;
    private List<RetailDeliveryOrderSummaryDto> orders;
}
