package com.suguna.weighment_api_service.admin.Projection;

import java.math.BigDecimal;

public interface DailyReportProjection {

    String getSchCode();
    String getFarmerName();
    String getFarmCode();
    String getCustomerName();
    String getCustomerCode();
    String getSupervisorCode();
    String getSupervisorName();
    String getDcNumber();
    String getSlipNo();
    String getWeighmentStartDate();
    String getWeighmentStartTime();
    String getWeighmentEndTime();
    String getVehicleNo();
    BigDecimal getWeightOfBirds();
    Integer getNoOfBirds();
    String getBirdType();
    BigDecimal getAvgWeight();
    Integer getErrorCount();
    Integer getAbortCount();
    BigDecimal getFeedStockBag();
    String getVehicleIn();
    String getVehicleOut();
    String getBranchCode();
    String getSchDate();
    String getDeliveryStartTime();
    String getDeliveryEndTime();
}
