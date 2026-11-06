package com.suguna.weighment_api_service.dto.transaction;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SubmitCompletedWeighmentResponse {

    private boolean success;

    private String message;

    private String scheduleId;

    /** e.g. RECEIVED */
    private String status;

    private String receivedAt;
}