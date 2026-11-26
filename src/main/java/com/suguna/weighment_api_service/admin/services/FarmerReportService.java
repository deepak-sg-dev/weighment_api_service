package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.requests.FarmerReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerReportListResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerReportResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface FarmerReportService {

    FarmerReportListResponse getFarmerReport(FarmerReportFilterRequest filter);

    List<FarmerReportResponse> getAllFarmerReportForExport(FarmerReportFilterRequest filter);

    ByteArrayInputStream exportFarmerReportToExcel(List<FarmerReportResponse> list, FarmerReportFilterRequest filter);

    ByteArrayInputStream exportFarmerReportToPdf(List<FarmerReportResponse> list, FarmerReportFilterRequest filter);
}
