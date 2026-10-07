package com.suguna.weighment_api_service.controller;

import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionActionResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.service.TransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weighment/transactions")
@Tag(name = "Weighment Transactions", description = "Completion submit, acknowledgement, retry and correction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<SubmitCompletedWeighmentResponse> submitCompletedWeighment(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @Valid @RequestBody SubmitCompletedWeighmentRequest request) {
        return ResponseEntity.ok(transactionService.submitCompletedWeighment(supervisor, request));
    }

    @GetMapping("/{transactionId}/acknowledgement")
    public ResponseEntity<TransactionAcknowledgementResponse> getAcknowledgement(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getAcknowledgement(supervisor, transactionId));
    }

    @PostMapping("/{transactionId}/retry")
    public ResponseEntity<TransactionActionResponse> retryTransaction(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String transactionId,
            @RequestBody TransactionRetryRequest request) {
        return ResponseEntity.ok(transactionService.retryTransaction(supervisor, transactionId, request));
    }

    @PostMapping("/{transactionId}/correction")
    public ResponseEntity<TransactionActionResponse> submitCorrection(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String transactionId,
            @Valid @RequestBody TransactionCorrectionRequest request) {
        return ResponseEntity.ok(transactionService.submitCorrection(supervisor, transactionId, request));
    }
}
