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
public class RetailDeliveryOrderDetailDto {

    private String retailOrderId;
    private String scheduleId;
    private String retailerId;
    private String retailerName;
    private String retailerMobile;
    private String retailerAddress;
    private String salesOrderNo;
    private String itemName;
    private double orderedQuantity;
    private int orderedBirdCount;
    private String status;
    private String branchCode;
    private String regionCode;
}