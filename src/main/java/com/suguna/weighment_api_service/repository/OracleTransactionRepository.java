package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;
import com.suguna.weighment_api_service.integration.oracle.TransactionSqlClient;
import org.springframework.stereotype.Repository;

@Repository
public class OracleTransactionRepository implements TransactionRepository {

    private final TransactionSqlClient transactionSqlClient;

    public OracleTransactionRepository(
            TransactionSqlClient transactionSqlClient) {

        this.transactionSqlClient = transactionSqlClient;
    }

    @Override
    public TransactionAcknowledgementResponse getAcknowledgement(
            String transactionId) {

        return transactionSqlClient.getAcknowledgement(transactionId);
    }

    @Override
    public void retryTransaction(
            String transactionId,
            TransactionRetryRequest request) {

        transactionSqlClient.retryTransaction(
                transactionId,
                request);
    }

    @Override
    public void submitCorrection(
            String transactionId,
            TransactionCorrectionRequest request) {

        transactionSqlClient.submitCorrection(
                transactionId,
                request
        );
    }
}