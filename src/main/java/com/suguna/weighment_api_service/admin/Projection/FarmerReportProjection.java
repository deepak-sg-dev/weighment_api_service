package com.suguna.weighment_api_service.admin.Projection;

import java.math.BigDecimal;

public interface FarmerReportProjection {

    String getSite();
    String getCustomer();
    String getSupervisor();
    String getDcNo();
    String getStartDate();
    String getStartTime();
    String getEndTime();
    String getVehicleNo();
    BigDecimal getWeight();
    Integer getBirds();
    BigDecimal getAvgWeight();
    Integer getErrCount();
    String getSchDate();
}
