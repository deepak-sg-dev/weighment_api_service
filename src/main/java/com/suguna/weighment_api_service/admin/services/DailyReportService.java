package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.DailyReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.DailyReportListResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.DailyReportResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface DailyReportService {

    DailyReportListResponse getDailyReport(DailyReportFilterRequest filter);

    List<DailyReportResponse> getAllDailyReportForExport(DailyReportFilterRequest filter);

    ByteArrayInputStream exportDailyReportToExcel(List<DailyReportResponse> list, DailyReportFilterRequest filter);

    ByteArrayInputStream exportDailyReportToPdf(List<DailyReportResponse> list, DailyReportFilterRequest filter);
}
