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
public class RetailDeliveryOrderDetailResponse {

    private boolean success;
    private RetailDeliveryOrderDetailDto order;
}