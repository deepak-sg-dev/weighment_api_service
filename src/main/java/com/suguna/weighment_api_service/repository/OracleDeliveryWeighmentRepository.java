package com.suguna.weighment_api_service.repository;

import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentRequest;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentResponse;
import com.suguna.weighment_api_service.integration.oracle.DeliveryWeighmentSqlClient;
import org.springframework.stereotype.Repository;

@Repository
public class OracleDeliveryWeighmentRepository implements DeliveryWeighmentRepository {

    private final DeliveryWeighmentSqlClient deliveryWeighmentSqlClient;

    public OracleDeliveryWeighmentRepository(DeliveryWeighmentSqlClient deliveryWeighmentSqlClient) {
        this.deliveryWeighmentSqlClient = deliveryWeighmentSqlClient;
    }

    @Override
    public SubmitDeliveryWeighmentResponse submitWeighment(
            RetailScheduleContext schedule, SubmitDeliveryWeighmentRequest request) {
        return deliveryWeighmentSqlClient.submitWeighment(schedule, request);
    }

    @Override
    public void recordShopPhoto(RetailScheduleContext schedule, String orderId, String fileKey) {
        deliveryWeighmentSqlClient.recordShopPhoto(schedule, orderId, fileKey);
    }

    @Override
    public void recordCustomerAcknowledgement(
            RetailScheduleContext schedule, String orderId, String signatureKey, String customerPhotoKey) {
        deliveryWeighmentSqlClient.recordCustomerAcknowledgement(
                schedule, orderId, signatureKey, customerPhotoKey);
    }

    @Override
    public void recordSignaturePhoto(RetailScheduleContext schedule, String orderId, String fileKey) {
        deliveryWeighmentSqlClient.recordSignaturePhoto(schedule, orderId, fileKey);
    }

    @Override
    public void recordCustomerPhoto(RetailScheduleContext schedule, String orderId, String fileKey) {
        deliveryWeighmentSqlClient.recordCustomerPhoto(schedule, orderId, fileKey);
    }
}
