package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.DailyReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.DailyReportListResponse;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.DailyReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

        DailyReportFilterRequest filter = DailyReportFilterRequest.builder()
                .reportDate(date)
                .fromDate(fromDate)
                .toDate(toDate)
                .branchCode(branchCode)
                .farmCode(farmCode)
                .page(page)
                .limit(limit)
                .build();

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
}
