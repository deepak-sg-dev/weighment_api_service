package com.suguna.weighment_api_service.admin.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportFilterRequest {

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate reportDate;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fromDate;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate toDate;

    private String branchCode;
    private String farmCode;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer limit = 20;
}
