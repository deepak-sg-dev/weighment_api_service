package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.RetailScheduleFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.RetailScheduleReportResponse;
import com.suguna.weighment_api_service.admin.services.RetailScheduleReportService;
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
@RequestMapping("/api/admin/reports/retail-schedule")
@RequiredArgsConstructor
public class RetailScheduleReportController {

    private final RetailScheduleReportService retailScheduleReportService;

    @GetMapping
    public ResponseEntity<List<RetailScheduleReportResponse>> getRetailScheduleReport(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(required = false) String npickScheduleNo,
            @RequestParam(required = false) String erpScheduleNo,
            @RequestParam(defaultValue = "All") String status,
            @RequestParam(required = false) String branchCode) {

        RetailScheduleFilterRequest filter = buildFilter(fromDate, toDate, npickScheduleNo, erpScheduleNo, status, branchCode);
        return ResponseEntity.ok(retailScheduleReportService.getRetailScheduleReport(filter));
    }

    @PostMapping("/search")
    public ResponseEntity<List<RetailScheduleReportResponse>> searchRetailScheduleReport(
            @Valid @RequestBody RetailScheduleFilterRequest filter) {

        return ResponseEntity.ok(retailScheduleReportService.getRetailScheduleReport(filter));
    }

    // ==================== EXCEL EXPORT ====================

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportRetailScheduleExcelGet(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(required = false) String npickScheduleNo,
            @RequestParam(required = false) String erpScheduleNo,
            @RequestParam(defaultValue = "All") String status,
            @RequestParam(required = false) String branchCode) {

        RetailScheduleFilterRequest filter = buildFilter(fromDate, toDate, npickScheduleNo, erpScheduleNo, status, branchCode);
        return buildExcelResponse(filter);
    }

    @PostMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportRetailScheduleExcelPost(
            @Valid @RequestBody RetailScheduleFilterRequest filter) {

        return buildExcelResponse(filter);
    }

    // ==================== PDF EXPORT ====================

    @GetMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportRetailSchedulePdfGet(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate toDate,
            @RequestParam(required = false) String npickScheduleNo,
            @RequestParam(required = false) String erpScheduleNo,
            @RequestParam(defaultValue = "All") String status,
            @RequestParam(required = false) String branchCode) {

        RetailScheduleFilterRequest filter = buildFilter(fromDate, toDate, npickScheduleNo, erpScheduleNo, status, branchCode);
        return buildPdfResponse(filter);
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportRetailSchedulePdfPost(
            @Valid @RequestBody RetailScheduleFilterRequest filter) {

        return buildPdfResponse(filter);
    }

    // ==================== PRIVATE HELPERS ====================

    private RetailScheduleFilterRequest buildFilter(
            LocalDate fromDate, LocalDate toDate, String npickScheduleNo,
            String erpScheduleNo, String status, String branchCode) {

        return RetailScheduleFilterRequest.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .npickScheduleNo(npickScheduleNo)
                .erpScheduleNo(erpScheduleNo)
                .status(status)
                .branchCode(branchCode)
                .build();
    }

    private ResponseEntity<InputStreamResource> buildExcelResponse(RetailScheduleFilterRequest filter) {
        List<RetailScheduleReportResponse> data = retailScheduleReportService.getRetailScheduleReport(filter);
        ByteArrayInputStream stream = retailScheduleReportService.exportRetailScheduleToExcel(data, filter);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Retail_Schedule_Report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    private ResponseEntity<InputStreamResource> buildPdfResponse(RetailScheduleFilterRequest filter) {
        List<RetailScheduleReportResponse> data = retailScheduleReportService.getRetailScheduleReport(filter);
        ByteArrayInputStream stream = retailScheduleReportService.exportRetailScheduleToPdf(data, filter);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Retail_Schedule_Report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(stream));
    }
}
