package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.RetailScheduleFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.RetailScheduleReportResponse;

import java.util.List;

public interface RetailScheduleReportService {

    List<RetailScheduleReportResponse> getRetailScheduleReport(RetailScheduleFilterRequest filter);
}
