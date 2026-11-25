package com.suguna.weighment_api_service.admin.controller;

import com.suguna.weighment_api_service.admin.dtos.requests.FarmerReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerReportListResponse;
import com.suguna.weighment_api_service.admin.response.ApiResponse;
import com.suguna.weighment_api_service.admin.response.Response;
import com.suguna.weighment_api_service.admin.services.FarmerReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

        FarmerReportFilterRequest filter = FarmerReportFilterRequest.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .branchCode(branchCode)
                .farmCode(farmCode)
                .page(page)
                .limit(limit)
                .build();

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
}
