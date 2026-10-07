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

    /** ACCEPTED, PENDING, REJECTED — ERP processing state. */
    private String status;

    private String localTransactionId;

    private String erpTransactionReference;

    private String acknowledgedAt;
}
