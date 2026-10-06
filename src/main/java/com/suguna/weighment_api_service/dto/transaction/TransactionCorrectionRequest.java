package com.suguna.weighment_api_service.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCorrectionRequest {

    private String reason;

    private Corrections corrections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Corrections {

        private String remarks;
    }
}
