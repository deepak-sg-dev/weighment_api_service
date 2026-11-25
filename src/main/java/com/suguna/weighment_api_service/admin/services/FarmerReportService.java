package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.FarmerReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerReportListResponse;

public interface FarmerReportService {

    FarmerReportListResponse getFarmerReport(FarmerReportFilterRequest filter);
}
