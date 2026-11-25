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
public class DailyReportResponse {

    private String schCode;              // blo.ERPINDENTID / wm.SCHEDULE_NO
    private String farmerName;           // blo.FARMERNAME
    private String farmCode;             // blo.FARMERCODE
    private String customerName;         // COALESCE(hp.PARTY_NAME, blo.CUS_NAME)
    private String customerCode;         // COALESCE(rod.CUSTOMER_CODE, blo.CUSTOMERCODE)
    private String supervisorCode;       // wm.WM_SUPERVISOR_CODE
    private String supervisorName;       // COALESCE(wm.WM_SUPERVISOR_NAME, dev.EMP_NAME)
    private String dcNumber;             // wm.REF_ID
    private String slipNo;               // NULL (not present in table)
    private String weighmentStartDate;   // wm.DC_DATE (dd-MM-yyyy)
    private String weighmentStartTime;   // wm.WM_START_TIME (HH24:MI:SS)
    private String weighmentEndTime;     // wm.WM_END_TIME (HH24:MI:SS)
    private String vehicleNo;            // COALESCE(blo.VEHICLENO, wm.VEHICLE_NO)
    private BigDecimal weightOfBirds;    // blo.NO_OF_KGS
    private Integer noOfBirds;           // blo.NO_OF_BIRDS
    private String birdType;             // wm.BIRD_TYPE
    private BigDecimal avgWeight;        // wm.AVG_WT
    private Integer errorCount;          // wm.ERRORCOUNT
    private Integer abortCount;          // wm.ABORTCOUNT
    private BigDecimal feedStockBag;     // wm.FEED_STOCK_BAGS
    private String vehicleIn;            // wm.VEHICLE_INTIME
    private String vehicleOut;           // wm.VEHICLE_OUTTIME
    private String branchCode;           // wm.BRANCH_CODE
    private String schDate;              // wm.ORDER_DATE (dd-MM-yyyy)
    private String deliveryStartTime;    // rod.DELV_START_DT
    private String deliveryEndTime;      // rod.DELV_END_DT
}
