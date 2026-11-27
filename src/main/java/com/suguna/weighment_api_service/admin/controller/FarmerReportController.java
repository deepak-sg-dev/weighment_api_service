package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.FarmerReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerReportListResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerReportResponse;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.FarmerReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports/farmer-report")
@RequiredArgsConstructor
public class FarmerReportController {

    private final FarmerReportService farmerReportService;

    @GetMapping
    public ResponseEntity<ApiResponse<FarmerReportListResponse>> getFarmerReport(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String farmCode,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        FarmerReportFilterRequest filter = buildFilter(fromDate, toDate, branchCode, farmCode, page, limit);

        return Response.success(
                "Farmer report fetched successfully",
                farmerReportService.getFarmerReport(filter)
        );
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<FarmerReportListResponse>> searchFarmerReport(
            @Valid @RequestBody FarmerReportFilterRequest filter) {

        return Response.success(
                "Farmer report fetched successfully",
                farmerReportService.getFarmerReport(filter)
        );
    }

    // ==================== EXCEL EXPORT ====================

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportFarmerReportExcelGet(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String farmCode) {

        FarmerReportFilterRequest filter = buildFilter(fromDate, toDate, branchCode, farmCode, null, null);
        return buildExcelResponse(filter);
    }

    @PostMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportFarmerReportExcelPost(
            @Valid @RequestBody FarmerReportFilterRequest filter) {

        return buildExcelResponse(filter);
    }

    // ==================== PDF EXPORT ====================

    @GetMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportFarmerReportPdfGet(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String farmCode) {

        FarmerReportFilterRequest filter = buildFilter(fromDate, toDate, branchCode, farmCode, null, null);
        return buildPdfResponse(filter);
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportFarmerReportPdfPost(
            @Valid @RequestBody FarmerReportFilterRequest filter) {

        return buildPdfResponse(filter);
    }

    // ==================== PRIVATE HELPERS ====================

    private FarmerReportFilterRequest buildFilter(
            LocalDate fromDate, LocalDate toDate, String branchCode,
            String farmCode, Integer page, Integer limit) {

        return FarmerReportFilterRequest.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .branchCode(branchCode)
                .farmCode(farmCode)
                .page(page)
                .limit(limit)
                .build();
    }

    private ResponseEntity<InputStreamResource> buildExcelResponse(FarmerReportFilterRequest filter) {
        List<FarmerReportResponse> data = farmerReportService.getAllFarmerReportForExport(filter);
        ByteArrayInputStream stream = farmerReportService.exportFarmerReportToExcel(data, filter);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Farmer_Report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    private ResponseEntity<InputStreamResource> buildPdfResponse(FarmerReportFilterRequest filter) {
        List<FarmerReportResponse> data = farmerReportService.getAllFarmerReportForExport(filter);
        ByteArrayInputStream stream = farmerReportService.exportFarmerReportToPdf(data, filter);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Farmer_Report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(stream));
    }
}
