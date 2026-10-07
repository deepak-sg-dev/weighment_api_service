package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionActionResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;
import com.suguna.weighment_api_service.integration.oracle.TransactionSqlClient;
import org.springframework.stereotype.Repository;

@Repository
public class OracleTransactionRepository implements TransactionRepository {

    private final TransactionSqlClient transactionSqlClient;

    public OracleTransactionRepository(TransactionSqlClient transactionSqlClient) {
        this.transactionSqlClient = transactionSqlClient;
    }

    @Override
    public SubmitCompletedWeighmentResponse submitCompleted(
            String supervisorCode, SubmitCompletedWeighmentRequest request) {
        return transactionSqlClient.submitCompleted(supervisorCode, request, "ACCEPTED");
    }

    @Override
    public TransactionAcknowledgementResponse getAcknowledgement(String transactionId) {
        return transactionSqlClient.getAcknowledgement(transactionId);
    }

    @Override
    public TransactionActionResponse retryTransaction(
            String transactionId, TransactionRetryRequest request) {
        transactionSqlClient.retryTransaction(transactionId, request);
        return TransactionActionResponse.builder()
                .success(true)
                .message("Retry submitted")
                .build();
    }

    @Override
    public TransactionActionResponse submitCorrection(
            String transactionId, TransactionCorrectionRequest request) {
        transactionSqlClient.submitCorrection(transactionId, request);
        return TransactionActionResponse.builder()
                .success(true)
                .message("Correction submitted")
                .build();
    }
}
