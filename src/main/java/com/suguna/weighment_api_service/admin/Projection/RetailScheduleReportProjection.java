package com.suguna.weighment_api_service.admin.Projection;

import java.math.BigDecimal;

public interface RetailScheduleReportProjection {
    String getScheduleDate();
    String getBranchName();
    String getSupervisor();
    String getFarmName();
    String getCustomerCode();
    String getCustomerName();
    String getCustomerAddress();
    BigDecimal getWeight();
    BigDecimal getQuantity();
    String getDeliveryStatus();
    String getDeliveryDate();
    String getDownloadLog();
    String getErpSchCode();
    String getStatus();
}
