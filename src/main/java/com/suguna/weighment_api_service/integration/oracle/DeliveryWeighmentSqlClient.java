package com.suguna.weighment_api_service.integration.oracle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentRequest;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentResponse;
import com.suguna.weighment_api_service.exception.ConflictException;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.constants.ErrorCode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeliveryWeighmentSqlClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private static final String UPDATE_ORDER_MESSAGE_SQL =
            """
            UPDATE sug_mai_ntail_order o
               SET o.message = ?,
                   o.uploaded_date = SYSDATE,
                   o.weight = NVL(?, o.weight),
                   o.quantity = NVL(?, o.quantity)
             WHERE TO_CHAR(o.order_no) = TRIM(?)
               AND TO_CHAR(o.customer_code) = TRIM(?)
               AND o.branch_code = ?
               AND o.region = ?
               AND TRUNC(o.trans_date) = TRUNC(?)
            """;

    private static final String UPDATE_ORDER_COMPLETE_SQL =
            """
            UPDATE sug_mai_ntail_order o
               SET o.uploaded_flag = 'Y',
                   o.message = ?,
                   o.uploaded_date = SYSDATE
             WHERE TO_CHAR(o.order_no) = TRIM(?)
               AND o.branch_code = ?
               AND o.region = ?
               AND TRUNC(o.trans_date) = TRUNC(?)
            """;

    private static final String LOAD_MESSAGE_SQL =
            """
            SELECT o.message, o.uploaded_flag
              FROM sug_mai_ntail_order o
             WHERE TO_CHAR(o.order_no) = TRIM(?)
               AND o.branch_code = ?
               AND o.region = ?
               AND TRUNC(o.trans_date) = TRUNC(?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RetailDeliverySqlClient retailDeliverySqlClient;

    public DeliveryWeighmentSqlClient(
            JdbcTemplate jdbcTemplate, RetailDeliverySqlClient retailDeliverySqlClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.retailDeliverySqlClient = retailDeliverySqlClient;
    }

    public SubmitDeliveryWeighmentResponse submitWeighment(
            RetailScheduleContext schedule, SubmitDeliveryWeighmentRequest request) {
        String orderId = request.getOrderId().trim();
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        validateCustomer(order.getRetailerId(), request);

        OrderMessageState state = loadMessageState(schedule, orderId);
        if (state.uploadedFlag() != null && "Y".equalsIgnoreCase(state.uploadedFlag())) {
            return receivedResponse(schedule.getScheduleId(), orderId, Instant.now());
        }
        if (DeliveryOrderMessageSupport.hasWeighmentReceived(state.message())) {
            return receivedResponse(schedule.getScheduleId(), orderId, Instant.now());
        }

        String payloadJson = serializePayload(request);
        String newMessage =
                DeliveryOrderMessageSupport.mergeWeighmentPayload(state.message(), orderId, payloadJson);

        Double netWeight = request.getTotals() != null ? request.getTotals().getTotalNet() : null;
        Integer totalBirds = request.getTotals() != null ? request.getTotals().getTotalBirds() : null;

        updateOrder(schedule, order.getRetailerId(), orderId, newMessage, netWeight, totalBirds);
        maybeMarkComplete(schedule, orderId, newMessage);

        return receivedResponse(schedule.getScheduleId(), orderId, Instant.now());
    }

    public void recordShopPhoto(RetailScheduleContext schedule, String orderId, String fileKey) {
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        OrderMessageState state = loadMessageState(schedule, orderId);
        String newMessage = DeliveryOrderMessageSupport.mergeShopPhoto(state.message(), orderId, fileKey);
        updateOrder(schedule, order.getRetailerId(), orderId, newMessage, null, null);
        maybeMarkComplete(schedule, orderId, newMessage);
    }

    public void recordCustomerAcknowledgement(
            RetailScheduleContext schedule, String orderId, String signatureKey, String customerPhotoKey) {
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        OrderMessageState state = loadMessageState(schedule, orderId);
        String newMessage = DeliveryOrderMessageSupport.mergeCustomerAck(
                state.message(), orderId, signatureKey, customerPhotoKey);
        updateOrder(schedule, order.getRetailerId(), orderId, newMessage, null, null);
        maybeMarkComplete(schedule, orderId, newMessage);
    }

    public void recordSignaturePhoto(RetailScheduleContext schedule, String orderId, String fileKey) {
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        OrderMessageState state = loadMessageState(schedule, orderId);
        String newMessage = DeliveryOrderMessageSupport.mergeSignatureKey(state.message(), orderId, fileKey);
        updateOrder(schedule, order.getRetailerId(), orderId, newMessage, null, null);
        maybeMarkComplete(schedule, orderId, newMessage);
    }

    public void recordCustomerPhoto(RetailScheduleContext schedule, String orderId, String fileKey) {
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        OrderMessageState state = loadMessageState(schedule, orderId);
        String newMessage = DeliveryOrderMessageSupport.mergeCustomerPhotoKey(state.message(), orderId, fileKey);
        updateOrder(schedule, order.getRetailerId(), orderId, newMessage, null, null);
        maybeMarkComplete(schedule, orderId, newMessage);
    }

    public boolean isWeighmentAlreadyReceived(RetailScheduleContext schedule, String orderId) {
        OrderMessageState state = loadMessageState(schedule, orderId);
        return DeliveryOrderMessageSupport.hasWeighmentReceived(state.message())
                || ("Y".equalsIgnoreCase(state.uploadedFlag()));
    }

    private void maybeMarkComplete(RetailScheduleContext schedule, String orderId, String message) {
        if (!DeliveryOrderMessageSupport.isComplete(message)) {
            return;
        }
        String finalMessage = DeliveryOrderMessageSupport.markUploadedComplete(message);
        Timestamp orderDate = Timestamp.from(schedule.getOrderDate());
        jdbcTemplate.update(
                UPDATE_ORDER_COMPLETE_SQL,
                finalMessage,
                orderId,
                schedule.getRetailBranchCode(),
                schedule.getRegionCode(),
                orderDate);
    }

    private void updateOrder(
            RetailScheduleContext schedule,
            String customerCode,
            String orderId,
            String message,
            Double weight,
            Integer quantity) {
        Timestamp orderDate = Timestamp.from(schedule.getOrderDate());
        int updated = jdbcTemplate.update(
                UPDATE_ORDER_MESSAGE_SQL,
                message,
                weight,
                quantity,
                orderId,
                customerCode,
                schedule.getRetailBranchCode(),
                schedule.getRegionCode(),
                orderDate);
        if (updated == 0) {
            throw new ResourceNotFoundException("Retail delivery order", orderId);
        }
    }

    private OrderMessageState loadMessageState(RetailScheduleContext schedule, String orderId) {
        Timestamp orderDate = Timestamp.from(schedule.getOrderDate());
        try {
            return jdbcTemplate.queryForObject(
                    LOAD_MESSAGE_SQL,
                    (rs, rowNum) -> new OrderMessageState(rs.getString("message"), rs.getString("uploaded_flag")),
                    orderId,
                    schedule.getRetailBranchCode(),
                    schedule.getRegionCode(),
                    orderDate);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Retail delivery order", orderId);
        }
    }

    private static void validateCustomer(String retailerId, SubmitDeliveryWeighmentRequest request) {
        if (request.getCustomer() == null || request.getCustomer().getCustomerCode() == null) {
            return;
        }
        String expected = retailerId == null ? "" : retailerId.trim();
        String provided = request.getCustomer().getCustomerCode().trim();
        if (!expected.equals(provided)) {
            throw new ConflictException(ErrorCode.CONFLICT, "customer.customerCode does not match retail order");
        }
    }

    private static String serializePayload(SubmitDeliveryWeighmentRequest request) {
        try {
            return MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException ex) {
            return "{\"serializationError\":true}";
        }
    }

    private static SubmitDeliveryWeighmentResponse receivedResponse(
            String scheduleId, String orderId, Instant receivedAt) {
        return SubmitDeliveryWeighmentResponse.builder()
                .success(true)
                .message("Delivery weighment received")
                .scheduleId(scheduleId)
                .orderId(orderId)
                .status("RECEIVED")
                .receivedAt(receivedAt.atOffset(ZoneOffset.UTC).format(ISO_INSTANT))
                .build();
    }

    private record OrderMessageState(String message, String uploadedFlag) {}
}
