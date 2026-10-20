package com.suguna.weighment_api_service.dto.retail;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetailDeliveryOrderListResponse {

    private boolean success;
    private String scheduleId;
    private List<RetailDeliveryOrderSummaryDto> orders;
}