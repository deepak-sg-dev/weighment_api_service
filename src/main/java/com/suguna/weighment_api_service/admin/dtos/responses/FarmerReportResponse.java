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
public class FarmerReportResponse {

    private String site;           // FARMERCODE
    private String customer;       // CUSTOMERCODE - CUS_NAME
    private String supervisor;     // EMP_NAME (from SUG_MAI_DEVICEDATA)
    private String dcNo;           // MANUAL_DC_NO
    private String startDate;      // DC_DATE (dd/MM/yyyy)
    private String startTime;      // WM_START_TIME
    private String endTime;        // WM_END_TIME
    private String vehicleNo;      // VEHICLE_NO
    private BigDecimal weight;     // NO_OF_KGS
    private Integer birds;         // NO_OF_BIRDS
    private BigDecimal avgWeight;  // AVG_WT
    private Integer errCount;      // ERRORCOUNT
    private String schDate;        // ORDER_DATE (dd-MM-yy)
}
