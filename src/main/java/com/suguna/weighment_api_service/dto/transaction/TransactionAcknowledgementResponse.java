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
public class TransactionAcknowledgementResponse {

    private boolean success;

    private String transactionId;

    private String status;

    private String erpTransactionReference;

    private String message;

    private String acknowledgedAt;
}