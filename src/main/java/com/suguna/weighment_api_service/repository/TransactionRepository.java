package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionActionResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;

public interface TransactionRepository {

    SubmitCompletedWeighmentResponse submitCompleted(
            String supervisorCode, SubmitCompletedWeighmentRequest request);

    TransactionAcknowledgementResponse getAcknowledgement(
            String transactionId);

    TransactionActionResponse retryTransaction(
            String transactionId,
            TransactionRetryRequest request);

    TransactionActionResponse submitCorrection(
            String transactionId,
            TransactionCorrectionRequest request);
}