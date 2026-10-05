package com.suguna.weighment_api_service.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAcknowledgementResponse {

    private boolean success;

    private String transactionId;

    private String status;

    private String message;

    private String acknowledgedAt;
}