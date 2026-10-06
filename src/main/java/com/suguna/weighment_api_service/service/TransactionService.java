package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionActionResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;
import com.suguna.weighment_api_service.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(
            TransactionRepository transactionRepository) {

        this.transactionRepository = transactionRepository;
    }

    public TransactionAcknowledgementResponse
    getAcknowledgement(
            String transactionId) {

        return transactionRepository
                .getAcknowledgement(transactionId);
    }

    public TransactionActionResponse retryTransaction(
            String transactionId,
            TransactionRetryRequest request) {

        transactionRepository.retryTransaction(
                transactionId,
                request);

        return TransactionActionResponse.builder()
                .success(true)
                .message("Retry submitted")
                .build();
    }

    public TransactionActionResponse submitCorrection(
            String transactionId,
            TransactionCorrectionRequest request) {

        transactionRepository.submitCorrection(
                transactionId,
                request);

        return TransactionActionResponse.builder()
                .success(true)
                .message("Correction submitted")
                .build();
    }
}