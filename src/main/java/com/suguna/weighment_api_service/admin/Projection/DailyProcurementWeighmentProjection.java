package com.suguna.weighment_api_service.admin.Projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DailyProcurementWeighmentProjection {
    String getScheduleNo();
    String getSchCodeDeviceDc();
    String getSupervisorName();
    String getDcNumber();
    String getSlipNo();
    LocalDateTime getStartDate();
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
    String getBirdType();
    BigDecimal getAvgWeight();
    Integer getErrorCount();
    Integer getAbortCount();
    BigDecimal getFeedStock();
    LocalDateTime getVehInTime();
    LocalDateTime getVehOutTime();
    String getEntryType();
    String getBranchName();
    String getSupervisorCode();
    LocalDateTime getSchDate();
    LocalDateTime getCapturedAt();
    String getStatus();
}
