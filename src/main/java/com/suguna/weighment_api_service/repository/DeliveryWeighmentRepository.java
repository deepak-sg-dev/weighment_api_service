package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.delivery.StartRetailDeliveryOrderRequest;
import com.suguna.weighment_api_service.dto.delivery.StartRetailDeliveryOrderResponse;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentRequest;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentResponse;

public interface DeliveryWeighmentRepository {

    StartRetailDeliveryOrderResponse startDeliveryOrder(
            RetailScheduleContext schedule, String orderId, StartRetailDeliveryOrderRequest request);

    SubmitDeliveryWeighmentResponse submitWeighment(
            RetailScheduleContext schedule, SubmitDeliveryWeighmentRequest request);

    void assertReadyForDeliveryEvidence(RetailScheduleContext schedule, String orderId);

    void recordShopPhoto(RetailScheduleContext schedule, String orderId, String fileKey);

    void recordCustomerAcknowledgement(
            RetailScheduleContext schedule, String orderId, String signatureKey, String customerPhotoKey);

    void recordSignaturePhoto(RetailScheduleContext schedule, String orderId, String fileKey);

    void recordCustomerPhoto(RetailScheduleContext schedule, String orderId, String fileKey);
}
