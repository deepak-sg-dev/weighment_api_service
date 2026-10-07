package com.suguna.weighment_api_service.dto.retail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailLocationDto {

    private String retailerId;
    private String retailerName;
    private String retailOrderId;
    private double latitude;
    private double longitude;
    private String address;
}
