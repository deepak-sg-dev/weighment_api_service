package com.suguna.weighment_api_service.controller;

import com.suguna.weighment_api_service.dto.delivery.DeliveryCustomerAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.delivery.DeliveryShopPhotoResponse;
import com.suguna.weighment_api_service.dto.delivery.StartRetailDeliveryOrderRequest;
import com.suguna.weighment_api_service.dto.delivery.StartRetailDeliveryOrderResponse;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentRequest;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentResponse;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.service.DeliveryWeighmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/weighment/deliveries")
@Tag(name = "Retail Delivery Weighment", description = "Per-order delivery weighment submit and media uploads")
public class WeighmentDeliveryController {

    private final DeliveryWeighmentService deliveryWeighmentService;

    public WeighmentDeliveryController(DeliveryWeighmentService deliveryWeighmentService) {
        this.deliveryWeighmentService = deliveryWeighmentService;
    }

    @PostMapping("/{orderId}/start")
    public ResponseEntity<StartRetailDeliveryOrderResponse> startRetailDeliveryOrder(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String orderId,
            @RequestParam String scheduleId,
            @Valid @RequestBody StartRetailDeliveryOrderRequest request) {
        return ResponseEntity.ok(
                deliveryWeighmentService.startRetailDeliveryOrder(supervisor, orderId, scheduleId, request));
    }

    @PostMapping
    public ResponseEntity<SubmitDeliveryWeighmentResponse> submitDeliveryWeighment(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @Valid @RequestBody SubmitDeliveryWeighmentRequest request) {
        return ResponseEntity.ok(deliveryWeighmentService.submitDeliveryWeighment(supervisor, request));
    }

    @PostMapping(value = "/{orderId}/shop-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeliveryShopPhotoResponse> uploadShopPhoto(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String orderId,
            @RequestParam String scheduleId,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant capturedAt) {
        return ResponseEntity.ok(
                deliveryWeighmentService.uploadShopPhoto(supervisor, orderId, scheduleId, photo, capturedAt));
    }

    @PostMapping(value = "/{orderId}/customer-acknowledgement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeliveryCustomerAcknowledgementResponse> uploadCustomerAcknowledgement(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String orderId,
            @RequestParam String scheduleId,
            @RequestParam("signature") MultipartFile signature,
            @RequestParam("customerPhoto") MultipartFile customerPhoto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant signedAt) {
        return ResponseEntity.ok(deliveryWeighmentService.uploadCustomerAcknowledgement(
                supervisor, orderId, scheduleId, signature, customerPhoto, signedAt));
    }
}
