package com.suguna.weighment_api_service.controller;

import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionActionResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;
import com.suguna.weighment_api_service.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weighment/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService) {

        this.transactionService = transactionService;
    }

    @GetMapping("/{transactionId}/acknowledgement")
    public ResponseEntity<TransactionAcknowledgementResponse>
    getAcknowledgement(
            @PathVariable String transactionId) {

        return ResponseEntity.ok(
                transactionService.getAcknowledgement(
                        transactionId));
    }

    @PostMapping("/{transactionId}/retry")
    public ResponseEntity<TransactionActionResponse>
    retryTransaction(
            @PathVariable String transactionId,
            @RequestBody TransactionRetryRequest request) {

        transactionService.retryTransaction(
                transactionId,
                request);

        return ResponseEntity.ok(
                TransactionActionResponse.builder()
                        .success(true)
                        .message("Transaction queued for retry")
                        .build()
        );
    }

    @PostMapping("/{transactionId}/correction")
    public ResponseEntity<TransactionActionResponse>
    submitCorrection(
            @PathVariable String transactionId,
            @RequestBody TransactionCorrectionRequest request) {

        transactionService.submitCorrection(
                transactionId,
                request);

        return ResponseEntity.ok(
                TransactionActionResponse.builder()
                        .success(true)
                        .message("Correction submitted successfully")
                        .build()
        );
    }
}