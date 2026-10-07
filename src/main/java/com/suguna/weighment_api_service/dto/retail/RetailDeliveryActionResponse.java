package com.suguna.weighment_api_service.dto.retail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailDeliveryActionResponse {

    private boolean success;
    private String retailOrderId;
    private String status;
    private String message;
}
