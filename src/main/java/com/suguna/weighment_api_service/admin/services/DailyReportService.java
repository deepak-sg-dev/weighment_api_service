package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.DailyReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.DailyReportListResponse;

public interface DailyReportService {

    DailyReportListResponse getDailyReport(DailyReportFilterRequest filter);
}
