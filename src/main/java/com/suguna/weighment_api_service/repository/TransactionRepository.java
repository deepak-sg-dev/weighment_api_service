package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;

public interface TransactionRepository {

    TransactionAcknowledgementResponse getAcknowledgement(
            String transactionId);

    void retryTransaction(
            String transactionId,
            TransactionRetryRequest request);

    void submitCorrection(
            String transactionId,
            TransactionCorrectionRequest request);
}