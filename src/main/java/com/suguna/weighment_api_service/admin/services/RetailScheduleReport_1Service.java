package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.RetailScheduleFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.RetailScheduleReportResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface RetailScheduleReport_1Service {

    List<RetailScheduleReportResponse> getRetailScheduleReport(RetailScheduleFilterRequest filter);

    ByteArrayInputStream exportRetailScheduleToExcel(List<RetailScheduleReportResponse> list, RetailScheduleFilterRequest filter);

    ByteArrayInputStream exportRetailScheduleToPdf(List<RetailScheduleReportResponse> list, RetailScheduleFilterRequest filter);
}
