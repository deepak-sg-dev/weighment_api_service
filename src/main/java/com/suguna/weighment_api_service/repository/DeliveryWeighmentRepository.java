package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentRequest;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentResponse;

public interface DeliveryWeighmentRepository {

    SubmitDeliveryWeighmentResponse submitWeighment(
            RetailScheduleContext schedule, SubmitDeliveryWeighmentRequest request);

    void recordShopPhoto(RetailScheduleContext schedule, String orderId, String fileKey);

    void recordCustomerAcknowledgement(
            RetailScheduleContext schedule, String orderId, String signatureKey, String customerPhotoKey);

    void recordSignaturePhoto(RetailScheduleContext schedule, String orderId, String fileKey);

    void recordCustomerPhoto(RetailScheduleContext schedule, String orderId, String fileKey);
}
