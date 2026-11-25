package com.suguna.weighment_api_service.admin.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailScheduleFilterRequest {

    @NotNull(message = "From date is required")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate toDate;

    private String npickScheduleNo;
    private String erpScheduleNo;

    @Builder.Default
    private String status = "All";

    private String branchCode;
}
