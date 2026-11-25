package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportListResponse {

    private List<DailyReportResponse> items;
    private PaginationDTO pagination;
}
