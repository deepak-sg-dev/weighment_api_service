package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionActionResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;

public interface TransactionRepository {

    TransactionAcknowledgementResponse getAcknowledgement(
            String transactionId);

    TransactionActionResponse retryTransaction(
            String transactionId,
            TransactionRetryRequest request);

    TransactionActionResponse submitCorrection(
            String transactionId,
            TransactionCorrectionRequest request);
}