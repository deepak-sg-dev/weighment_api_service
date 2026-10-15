package com.suguna.weighment_api_service.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitCompletedWeighmentResponse {

    private boolean success;

    private String message;

    private String scheduleId;

    /** e.g. RECEIVED */
    private String status;

    private String receivedAt;
}
