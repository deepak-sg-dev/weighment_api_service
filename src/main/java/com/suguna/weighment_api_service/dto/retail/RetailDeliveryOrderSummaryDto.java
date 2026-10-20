package com.suguna.weighment_api_service.dto.retail;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RetailDeliveryOrderSummaryDto {

    private String retailOrderId;
    private String retailerId;
    private String retailerName;
    private String salesOrderNo;
    private double orderedQuantity;
    private int orderedBirdCount;
    private String status;
}