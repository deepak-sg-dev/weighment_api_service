package com.suguna.weighment_api_service.dto.retail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailDeliveryOrderSummaryDto {

    private String retailOrderId;
    private String retailerId;
    private String retailerName;
    private String salesOrderNo;
    private double orderedQuantity;
    private int orderedBirdCount;
    private String status;
}
