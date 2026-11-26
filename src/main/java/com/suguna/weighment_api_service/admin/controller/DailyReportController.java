package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.DailyReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.DailyReportListResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.DailyReportResponse;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.DailyReportService;
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
@RequestMapping("/api/admin/reports/daily-report")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;

    @GetMapping
    public ResponseEntity<ApiResponse<DailyReportListResponse>> getDailyReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String farmCode,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        DailyReportFilterRequest filter = buildFilter(date, fromDate, toDate, branchCode, farmCode, page, limit);

        return Response.success(
                "Daily report fetched successfully",
                dailyReportService.getDailyReport(filter)
        );
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<DailyReportListResponse>> searchDailyReport(
            @Valid @RequestBody DailyReportFilterRequest filter) {

        return Response.success(
                "Daily report fetched successfully",
                dailyReportService.getDailyReport(filter)
        );
    }

    // ==================== EXCEL EXPORT ====================

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportDailyReportExcelGet(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String farmCode) {

        DailyReportFilterRequest filter = buildFilter(date, fromDate, toDate, branchCode, farmCode, null, null);
        return buildExcelResponse(filter);
    }

    @PostMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportDailyReportExcelPost(
            @Valid @RequestBody DailyReportFilterRequest filter) {

        return buildExcelResponse(filter);
    }

    // ==================== PDF EXPORT ====================

    @GetMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportDailyReportPdfGet(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String farmCode) {

        DailyReportFilterRequest filter = buildFilter(date, fromDate, toDate, branchCode, farmCode, null, null);
        return buildPdfResponse(filter);
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportDailyReportPdfPost(
            @Valid @RequestBody DailyReportFilterRequest filter) {

        return buildPdfResponse(filter);
    }

    // ==================== PRIVATE HELPERS ====================

    private DailyReportFilterRequest buildFilter(
            LocalDate date, LocalDate fromDate, LocalDate toDate,
            String branchCode, String farmCode, Integer page, Integer limit) {

        return DailyReportFilterRequest.builder()
                .reportDate(date)
                .fromDate(fromDate)
                .toDate(toDate)
                .branchCode(branchCode)
                .farmCode(farmCode)
                .page(page)
                .limit(limit)
                .build();
    }

    private ResponseEntity<InputStreamResource> buildExcelResponse(DailyReportFilterRequest filter) {
        List<DailyReportResponse> data = dailyReportService.getAllDailyReportForExport(filter);
        ByteArrayInputStream stream = dailyReportService.exportDailyReportToExcel(data, filter);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Daily_Procurement_Report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    private ResponseEntity<InputStreamResource> buildPdfResponse(DailyReportFilterRequest filter) {
        List<DailyReportResponse> data = dailyReportService.getAllDailyReportForExport(filter);
        ByteArrayInputStream stream = dailyReportService.exportDailyReportToPdf(data, filter);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Daily_Procurement_Report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(stream));
    }
}
