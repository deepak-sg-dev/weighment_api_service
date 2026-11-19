package com.suguna.weighment_api_service.integration.oracle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.domain.DeliveryCompletedWeighmentHeader;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.delivery.StartRetailDeliveryOrderRequest;
import com.suguna.weighment_api_service.dto.delivery.StartRetailDeliveryOrderResponse;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentRequest;
import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentResponse;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderDetailDto;
import com.suguna.weighment_api_service.exception.ConflictException;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.constants.ErrorCodes;
import com.suguna.weighment_api_service.exception.BusinessValidationException;
import com.suguna.weighment_api_service.util.WeighmentSubmitKeys;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeliveryWeighmentSqlClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;
    private static final int START_ORACLE_TIMEOUT_SECONDS = 10;

    private final JdbcTemplate jdbcTemplate;
    private final OracleErpProperties erpProperties;
    private final RetailOrderLineSqlSupport retailOrderSql;
    private final RetailDeliverySqlClient retailDeliverySqlClient;
    private final LiftingScheduleSqlClient liftingScheduleSqlClient;
    private final WeighmentDetailSqlClient weighmentDetailSqlClient;
    private final WeighmentHeaderSqlClient weighmentHeaderSqlClient;
    private final String updateOrderMessageSql;
    private final String updateDeliveredQtySql;
    private final String startDeliveryOrderSql;
    private final String orderCompleteSql;
    private final String loadOrderStateSql;
    private final String loadOrderStateByShopSql;

    public DeliveryWeighmentSqlClient(
            JdbcTemplate jdbcTemplate,
            OracleErpProperties erpProperties,
            RetailOrderLineSqlSupport retailOrderSql,
            RetailDeliverySqlClient retailDeliverySqlClient,
            LiftingScheduleSqlClient liftingScheduleSqlClient,
            WeighmentDetailSqlClient weighmentDetailSqlClient,
            WeighmentHeaderSqlClient weighmentHeaderSqlClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.erpProperties = erpProperties;
        this.retailOrderSql = retailOrderSql;
        this.retailDeliverySqlClient = retailDeliverySqlClient;
        this.liftingScheduleSqlClient = liftingScheduleSqlClient;
        this.weighmentDetailSqlClient = weighmentDetailSqlClient;
        this.weighmentHeaderSqlClient = weighmentHeaderSqlClient;
        String existsSchedule = retailOrderSql.existsDetailLinkedToSchedule();
        this.updateOrderMessageSql =
                retailOrderSql.buildDetailUpdatePrefix()
                        + retailOrderSql.buildMessageUpdateSetClause()
                        + RetailOrderLineSqlSupport.updateWhereClause()
                        + RetailOrderLineSqlSupport.whereDetailOrderNo()
                        + retailOrderSql.andDetailCustomerCodeFilter().trim()
                        + " AND "
                        + existsSchedule;
        this.updateDeliveredQtySql =
                retailOrderSql.buildDetailUpdatePrefix()
                        + retailOrderSql.buildDeliveredQtyUpdateSetClause()
                        + RetailOrderLineSqlSupport.updateWhereClause()
                        + RetailOrderLineSqlSupport.whereDetailOrderNo()
                        + retailOrderSql.andDetailCustomerCodeFilter().trim()
                        + " AND "
                        + existsSchedule;
        this.startDeliveryOrderSql =
                retailOrderSql.buildDetailUpdatePrefix()
                        + retailOrderSql.buildStartDeliverySetClause()
                        + RetailOrderLineSqlSupport.updateWhereClause()
                        + RetailOrderLineSqlSupport.whereDetailOrderNoNumeric()
                        + RetailOrderLineSqlSupport.andDetailCustomerCodeBind().trim();
        this.orderCompleteSql =
                retailOrderSql.buildDetailUpdatePrefix()
                        + retailOrderSql.buildCompleteDetailSetClause()
                        + RetailOrderLineSqlSupport.updateWhereClause()
                        + RetailOrderLineSqlSupport.whereDetailOrderNo()
                        + " AND "
                        + existsSchedule;
        this.loadOrderStateSql = retailOrderSql.buildLoadOrderMessageStateSql();
        this.loadOrderStateByShopSql =
                retailOrderSql.buildLoadOrderMessageStateByShopSql() + "\n           FOR UPDATE WAIT 1";
    }

    public StartRetailDeliveryOrderResponse startDeliveryOrder(
            RetailScheduleContext schedule, String orderId, StartRetailDeliveryOrderRequest request) {
        String retailerId = request.getRetailerId() == null ? "" : request.getRetailerId().trim();
        OrderMessageState state = loadMessageStateByShop(orderId, retailerId);
        validateRetailer(state.customerCode(), retailerId);
        if (isOrderDeliveryComplete(state)) {
            throw new ConflictException(ErrorCode.CONFLICT, "Retail delivery order is already completed");
        }
        if (RetailOrderDeliveryStatus.isStartedOrBeyond(
                state.delvStatus(), state.message(), state.deliveryStartedAt())) {
            return startResponse(schedule.getScheduleId(), orderId, state.deliveryStartedAt(), true);
        }

        double lat = request.getLocation().getLatitude();
        double lon = request.getLocation().getLongitude();
        Instant startedAt = request.getStartedAt();
        String newMessage =
                DeliveryOrderMessageSupport.mergeDeliveryStart(state.message(), orderId, startedAt, lat, lon);

        int updated;
        try {
            updated =
                    updateWithTimeout(
                            startDeliveryOrderSql,
                            Timestamp.from(startedAt),
                            newMessage,
                            orderId,
                            state.customerCode());
        } catch (DataAccessException ex) {
            if (isStartLockOrTimeout(ex)) {
                throw startTimedOut(orderId);
            }
            throw ex;
        }
        if (updated == 0) {
            throw new ResourceNotFoundException("Retail delivery order", orderId);
        }
        if (updated > 1) {
            throw new ConflictException(
                    ErrorCode.CONFLICT,
                    "Delivery start matched " + updated + " shop lines; expected only order " + orderId);
        }

        liftingScheduleSqlClient.syncRetailDeliveryStatus(
                schedule.getSupervisorCode(),
                schedule.getScheduleId(),
                RetailOrderDeliveryStatus.toApiStatus(RetailOrderDeliveryStatus.IN_PROGRESS));
        return startResponse(schedule.getScheduleId(), orderId, startedAt, false);
    }

    public SubmitDeliveryWeighmentResponse submitWeighment(
            RetailScheduleContext schedule, SubmitDeliveryWeighmentRequest request) {
        String orderId = request.getOrderId().trim();
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        validateCustomer(order.getRetailerId(), request);

        OrderMessageState state = loadMessageState(schedule, orderId);
        ensureDeliveryStarted(state);

        String deliveryKey =
                WeighmentSubmitKeys.deliveryTransactionKey(schedule.getScheduleId(), orderId);
        boolean weighmentInMessage = DeliveryOrderMessageSupport.hasWeighmentReceived(state.message());
        boolean orderClosed = RetailOrderDeliveryStatus.isDeliveryComplete(state.uploadedFlag(), state.message());
        Double netWeight = request.getTotals() != null ? request.getTotals().getTotalNet() : null;
        Integer totalBirds = request.getTotals() != null ? request.getTotals().getTotalBirds() : null;

        if (!weighmentInMessage && !orderClosed) {
            String payloadJson = serializePayload(request);
            String newMessage =
                    DeliveryOrderMessageSupport.mergeWeighmentPayload(state.message(), orderId, payloadJson);
            updateOrder(schedule, order.getRetailerId(), orderId, newMessage, netWeight, totalBirds);
            maybeMarkComplete(schedule, orderId, newMessage);
        } else if (netWeight != null || totalBirds != null) {
            updateDeliveredQuantities(schedule, order.getRetailerId(), orderId, netWeight, totalBirds);
        }

        OrderMessageState afterCompleteCheck = loadMessageState(schedule, orderId);
        if (isOrderDeliveryComplete(afterCompleteCheck)) {
            persistDeliveryHeaderAndDetails(schedule, order, orderId, deliveryKey, request);
        }

        syncLiftingDeliveryStatus(schedule);

        OrderMessageState after = loadMessageState(schedule, orderId);
        String deliveryStatus = RetailOrderDeliveryStatus.resolveForOrder(
                after.delvStatus(), after.uploadedFlag(), after.message(), after.deliveryStartedAt());
        return receivedResponse(schedule.getScheduleId(), orderId, Instant.now(), deliveryStatus);
    }

    private boolean isOrderDeliveryComplete(OrderMessageState state) {
        if (state.delvStatus() != null && "COMPLETED".equalsIgnoreCase(state.delvStatus().trim())) {
            return true;
        }
        return RetailOrderDeliveryStatus.isDeliveryComplete(state.uploadedFlag(), state.message());
    }

    private void ensureDeliveryStarted(OrderMessageState state) {
        if (!erpProperties.isRetailOrderDeliveryRequireStart()) {
            return;
        }
        if (RetailOrderDeliveryStatus.isStartedOrBeyond(
                state.delvStatus(), state.message(), state.deliveryStartedAt())) {
            return;
        }
        throw new BusinessValidationException(
                ErrorCodes.VALIDATION_PENDING,
                "Start retail delivery for this order before uploading evidence or submitting weighment");
    }

    /** Validates order exists and delivery has been started when {@code retail-order-delivery-require-start} is on. */
    public void assertReadyForDeliveryEvidence(RetailScheduleContext schedule, String orderId) {
        retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        OrderMessageState state = loadMessageState(schedule, orderId);
        ensureDeliveryStarted(state);
    }

    private void persistDeliveryHeaderAndDetails(
            RetailScheduleContext schedule,
            RetailDeliveryOrderDetailDto order,
            String orderId,
            String deliveryKey,
            SubmitDeliveryWeighmentRequest request) {
        long transId = persistCompletedDeliveryHeader(schedule, order, orderId, request);
        weighmentDetailSqlClient.insertDeliveryWeighmentDetails(
                schedule.getSupervisorCode(),
                schedule.getScheduleId(),
                transId,
                orderId,
                order.getRetailerId(),
                deliveryKey,
                request);
    }

    long persistCompletedDeliveryHeader(
            RetailScheduleContext schedule,
            RetailDeliveryOrderDetailDto order,
            String orderId,
            SubmitDeliveryWeighmentRequest request) {
        DeliveryCompletedWeighmentHeader.DeliveryCompletedWeighmentHeaderBuilder header =
                DeliveryCompletedWeighmentHeader.builder()
                        .scheduleId(schedule.getScheduleId())
                        .supervisorCode(schedule.getSupervisorCode())
                        .retailOrderNo(orderId)
                        .customerCode(order.getRetailerId())
                        .customerName(order.getRetailerName())
                        .orderedQty(order.getOrderedQuantity());
        if (request != null) {
            var totals = request.getTotals();
            if (totals != null) {
                header.noOfBirds(totals.getTotalBirds())
                        .noOfCages(totals.getTotalCagesOrBatches())
                        .emptyWt(totals.getTotalTare())
                        .grossWt(totals.getTotalGross())
                        .netWt(totals.getTotalNet())
                        .avgWt(totals.getOverallAverageBirdWeight());
            }
            var shop = request.getShopValidation();
            if (shop != null) {
                header.latitude(shop.getLatitude()).longitude(shop.getLongitude());
            }
            var timeline = request.getTimeline();
            if (timeline != null) {
                header.wmStartTime(timeline.getWeighmentStartedAt())
                        .wmEndTime(timeline.getWeighmentCompletedAt());
            }
            var payment = request.getPayment();
            if (payment != null) {
                header.manualDcNo(payment.getDcNumber());
            }
        }
        return weighmentHeaderSqlClient.ensureCompletedDeliveryHeader(header.build());
    }

    public void recordShopPhoto(RetailScheduleContext schedule, String orderId, String fileKey) {
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        OrderMessageState state = loadMessageState(schedule, orderId);
        ensureDeliveryStarted(state);
        String newMessage = DeliveryOrderMessageSupport.mergeShopPhoto(state.message(), orderId, fileKey);
        updateOrder(schedule, order.getRetailerId(), orderId, newMessage, null, null);
        maybeMarkComplete(schedule, orderId, newMessage);
        syncLiftingDeliveryStatus(schedule);
    }

    public void recordCustomerAcknowledgement(
            RetailScheduleContext schedule, String orderId, String signatureKey, String customerPhotoKey) {
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        OrderMessageState state = loadMessageState(schedule, orderId);
        ensureDeliveryStarted(state);
        String newMessage = DeliveryOrderMessageSupport.mergeCustomerAck(
                state.message(), orderId, signatureKey, customerPhotoKey);
        updateOrder(schedule, order.getRetailerId(), orderId, newMessage, null, null);
        maybeMarkComplete(schedule, orderId, newMessage);
        syncLiftingDeliveryStatus(schedule);
    }

    public void recordSignaturePhoto(RetailScheduleContext schedule, String orderId, String fileKey) {
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        OrderMessageState state = loadMessageState(schedule, orderId);
        ensureDeliveryStarted(state);
        String newMessage = DeliveryOrderMessageSupport.mergeSignatureKey(state.message(), orderId, fileKey);
        updateOrder(schedule, order.getRetailerId(), orderId, newMessage, null, null);
        maybeMarkComplete(schedule, orderId, newMessage);
        syncLiftingDeliveryStatus(schedule);
    }

    public void recordDeliverySupervisorSignaturePhoto(
            RetailScheduleContext schedule, String orderId, String fileKey) {
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        OrderMessageState state = loadMessageState(schedule, orderId);
        ensureDeliveryStarted(state);
        String newMessage =
                DeliveryOrderMessageSupport.mergeDeliverySupervisorSignatureKey(
                        state.message(), orderId, fileKey);
        updateOrder(schedule, order.getRetailerId(), orderId, newMessage, null, null);
        syncLiftingDeliveryStatus(schedule);
    }

    public void recordCustomerPhoto(RetailScheduleContext schedule, String orderId, String fileKey) {
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        OrderMessageState state = loadMessageState(schedule, orderId);
        ensureDeliveryStarted(state);
        String newMessage = DeliveryOrderMessageSupport.mergeCustomerPhotoKey(state.message(), orderId, fileKey);
        updateOrder(schedule, order.getRetailerId(), orderId, newMessage, null, null);
        maybeMarkComplete(schedule, orderId, newMessage);
        syncLiftingDeliveryStatus(schedule);
    }

    public boolean isWeighmentAlreadyReceived(RetailScheduleContext schedule, String orderId) {
        OrderMessageState state = loadMessageState(schedule, orderId);
        return DeliveryOrderMessageSupport.hasWeighmentReceived(state.message())
                || RetailOrderDeliveryStatus.isDeliveryComplete(state.uploadedFlag(), state.message());
    }

    private void maybeMarkComplete(RetailScheduleContext schedule, String orderId, String message) {
        if (!DeliveryOrderMessageSupport.isComplete(message)) {
            return;
        }
        String finalMessage = DeliveryOrderMessageSupport.markUploadedComplete(message);
        Object[] args = new Object[1 + 1 + 7];
        args[0] = finalMessage;
        args[1] = orderId;
        System.arraycopy(retailDeliverySqlClient.scheduleBindArgs(schedule), 0, args, 2, 7);
        jdbcTemplate.update(orderCompleteSql, args);
        var order = retailDeliverySqlClient.getDeliveryOrder(schedule, orderId);
        persistCompletedDeliveryHeader(schedule, order, orderId, null);
        syncLiftingDeliveryStatus(schedule);
    }

    private void syncLiftingDeliveryStatus(RetailScheduleContext schedule) {
        String aggregated = retailDeliverySqlClient.aggregateScheduleDeliveryStatus(schedule);
        liftingScheduleSqlClient.syncRetailDeliveryStatus(
                schedule.getSupervisorCode(), schedule.getScheduleId(), aggregated);
    }

    private void updateOrder(
            RetailScheduleContext schedule,
            String customerCode,
            String orderId,
            String message,
            Double deliveredWeight,
            Integer deliveredQuantity) {
        Object[] args = new Object[5 + 7];
        args[0] = message;
        args[1] = deliveredWeight;
        args[2] = deliveredQuantity;
        args[3] = orderId;
        args[4] = customerCode;
        System.arraycopy(retailDeliverySqlClient.scheduleBindArgs(schedule), 0, args, 5, 7);
        int updated = jdbcTemplate.update(updateOrderMessageSql, args);
        if (updated == 0) {
            throw new ResourceNotFoundException("Retail delivery order", orderId);
        }
    }

    private void updateDeliveredQuantities(
            RetailScheduleContext schedule,
            String customerCode,
            String orderId,
            Double deliveredWeight,
            Integer deliveredQuantity) {
        Object[] args = new Object[4 + 7];
        args[0] = deliveredWeight;
        args[1] = deliveredQuantity;
        args[2] = orderId;
        args[3] = customerCode;
        System.arraycopy(retailDeliverySqlClient.scheduleBindArgs(schedule), 0, args, 4, 7);
        int updated = jdbcTemplate.update(updateDeliveredQtySql, args);
        if (updated == 0) {
            throw new ResourceNotFoundException("Retail delivery order", orderId);
        }
    }

    private OrderMessageState loadMessageState(RetailScheduleContext schedule, String orderId) {
        return loadMessageStateByShop(orderId, null);
    }

    private OrderMessageState loadMessageStateByShop(String orderId, String customerCode) {
        try {
            if (customerCode != null && !customerCode.isBlank()) {
                return queryMessageState(loadOrderStateByShopSql, orderId, customerCode.trim());
            }
            return queryMessageState(loadOrderStateSql, orderId);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Retail delivery order", orderId);
        } catch (DataAccessException ex) {
            if (isStartLockOrTimeout(ex)) {
                throw startTimedOut(orderId);
            }
            throw ex;
        }
    }

    private OrderMessageState queryMessageState(String sql, Object... args) {
        return jdbcTemplate.execute((Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setQueryTimeout(START_ORACLE_TIMEOUT_SECONDS);
                bind(ps, args);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new EmptyResultDataAccessException(1);
                    }
                    return mapMessageState(rs, 0);
                }
            }
        });
    }

    private int updateWithTimeout(String sql, Object... args) {
        Integer updated = jdbcTemplate.execute((Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setQueryTimeout(START_ORACLE_TIMEOUT_SECONDS);
                bind(ps, args);
                return ps.executeUpdate();
            }
        });
        return updated == null ? 0 : updated;
    }

    private static void bind(PreparedStatement ps, Object... args) throws java.sql.SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    private static boolean isStartLockOrTimeout(DataAccessException ex) {
        if (ex instanceof QueryTimeoutException
                || ex instanceof CannotAcquireLockException
                || ex instanceof PessimisticLockingFailureException) {
            return true;
        }
        String message = ex.getMostSpecificCause() == null ? ex.getMessage() : ex.getMostSpecificCause().getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("ORA-30006")
                || message.contains("ORA-00054")
                || message.contains("timed out");
    }

    private static ConflictException startTimedOut(String orderId) {
        return new ConflictException(
                ErrorCode.CONFLICT, "Delivery start timed out for order " + orderId + "; retry");
    }

    private OrderMessageState mapMessageState(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Timestamp started = rs.getTimestamp("delivery_started_date");
        Instant startedInstant = started == null ? null : started.toInstant();
        return new OrderMessageState(
                rs.getString("customer_code"),
                rs.getString("message"),
                rs.getString("uploaded_flag"),
                rs.getString("delv_status"),
                startedInstant);
    }

    private static void validateRetailer(String expectedRetailerId, String providedRetailerId) {
        String expected = expectedRetailerId == null ? "" : expectedRetailerId.trim();
        String provided = providedRetailerId == null ? "" : providedRetailerId.trim();
        if (!expected.equals(provided)) {
            throw new ConflictException(ErrorCode.CONFLICT, "retailerId does not match retail order");
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

    private static StartRetailDeliveryOrderResponse startResponse(
            String scheduleId, String orderId, Instant startedAt, boolean idempotentReplay) {
        Instant effective = startedAt == null ? Instant.now() : startedAt;
        String deliveryStatus = RetailOrderDeliveryStatus.toApiStatus(RetailOrderDeliveryStatus.IN_PROGRESS);
        return StartRetailDeliveryOrderResponse.builder()
                .success(true)
                .scheduleId(scheduleId)
                .orderId(orderId)
                .deliveryStatus(deliveryStatus)
                .status(deliveryStatus)
                .startedAt(effective.atOffset(ZoneOffset.UTC).format(ISO_INSTANT))
                .message(idempotentReplay ? "Delivery already started for this order" : "Retail delivery started")
                .build();
    }

    private static SubmitDeliveryWeighmentResponse receivedResponse(
            String scheduleId, String orderId, Instant receivedAt, String deliveryStatus) {
        return SubmitDeliveryWeighmentResponse.builder()
                .success(true)
                .message("Delivery weighment received")
                .scheduleId(scheduleId)
                .orderId(orderId)
                .status("RECEIVED")
                .deliveryStatus(deliveryStatus)
                .receivedAt(receivedAt.atOffset(ZoneOffset.UTC).format(ISO_INSTANT))
                .build();
    }

    private record OrderMessageState(
            String customerCode,
            String message,
            String uploadedFlag,
            String delvStatus,
            Instant deliveryStartedAt) {}
}
