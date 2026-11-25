package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailScheduleReportResponse {

    private String scheduleDate;
    private String branchName;
    private String supervisor;
    private String farmName;
    private String customerCode;
    private String customerName;
    private String customerAddress;
    private BigDecimal weight;
    private BigDecimal quantity;
    private String deliveryStatus;
    private String deliveryDate;
    private String downloadLog;
    private String erpSchCode;
    private String status;
}
