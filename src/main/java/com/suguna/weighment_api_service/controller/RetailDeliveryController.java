package com.suguna.weighment_api_service.controller;

import com.suguna.weighment_api_service.dto.retail.CompleteRetailDeliveryRequest;
import com.suguna.weighment_api_service.dto.retail.CompleteRetailDeliveryResponse;
import com.suguna.weighment_api_service.dto.retail.MarkRetailerArrivedRequest;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryActionResponse;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderDetailResponse;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderListResponse;
import com.suguna.weighment_api_service.dto.retail.RetailInvoiceStatusResponse;
import com.suguna.weighment_api_service.dto.retail.RetailLocationsResponse;
import com.suguna.weighment_api_service.dto.retail.TriggerRetailInvoiceRequest;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.service.RetailDeliveryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/retail")
@Tag(name = "Retail Delivery", description = "Retail delivery orders, arrival, completion and invoice sync")
public class RetailDeliveryController {

    private final RetailDeliveryService retailDeliveryService;

    public RetailDeliveryController(RetailDeliveryService retailDeliveryService) {
        this.retailDeliveryService = retailDeliveryService;
    }

    @GetMapping("/delivery-orders")
    public ResponseEntity<RetailDeliveryOrderListResponse> listDeliveryOrders(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam String scheduleId) {
        return ResponseEntity.ok(retailDeliveryService.listDeliveryOrders(supervisor, scheduleId));
    }

    @GetMapping("/delivery-orders/{retailOrderId}")
    public ResponseEntity<RetailDeliveryOrderDetailResponse> getDeliveryOrder(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String retailOrderId,
            @RequestParam String scheduleId) {
        return ResponseEntity.ok(retailDeliveryService.getDeliveryOrder(supervisor, retailOrderId, scheduleId));
    }

    @GetMapping("/locations")
    public ResponseEntity<RetailLocationsResponse> listRetailerLocations(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @RequestParam String scheduleId) {
        return ResponseEntity.ok(retailDeliveryService.listRetailerLocations(supervisor, scheduleId));
    }

    @PostMapping("/delivery-orders/{retailOrderId}/arrival")
    public ResponseEntity<RetailDeliveryActionResponse> markArrived(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String retailOrderId,
            @RequestParam String scheduleId,
            @Valid @RequestBody MarkRetailerArrivedRequest request) {
        return ResponseEntity.ok(retailDeliveryService.markArrived(supervisor, retailOrderId, scheduleId, request));
    }

    @PostMapping("/delivery-orders/{retailOrderId}/complete")
    public ResponseEntity<CompleteRetailDeliveryResponse> completeDelivery(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String retailOrderId,
            @Valid @RequestBody CompleteRetailDeliveryRequest request) {
        return ResponseEntity.ok(retailDeliveryService.completeDelivery(supervisor, retailOrderId, request));
    }

    @PostMapping("/delivery-orders/{retailOrderId}/invoice")
    public ResponseEntity<CompleteRetailDeliveryResponse> triggerInvoice(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String retailOrderId,
            @RequestBody(required = false) TriggerRetailInvoiceRequest request) {
        return ResponseEntity.ok(
                retailDeliveryService.triggerInvoice(
                        supervisor, retailOrderId, request == null ? new TriggerRetailInvoiceRequest() : request));
    }

    @GetMapping("/delivery-orders/{retailOrderId}/invoice-status")
    public ResponseEntity<RetailInvoiceStatusResponse> getInvoiceStatus(
            @AuthenticationPrincipal AuthenticatedSupervisor supervisor,
            @PathVariable String retailOrderId) {
        return ResponseEntity.ok(retailDeliveryService.getInvoiceStatus(supervisor, retailOrderId));
    }
}
