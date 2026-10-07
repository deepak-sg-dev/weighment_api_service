package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class TransactionSqlClient {

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private static final String WEIGHMENT_SUPERVISOR_MATCH =
            """
             (TRIM(w.wm_supervisor_code) = TRIM(?)
              OR LTRIM(TRIM(w.wm_supervisor_code), '0') = LTRIM(TRIM(?), '0'))
            """;

    private static final String ACKNOWLEDGEMENT_SQL =
            """
            SELECT w.trans_id,
                   w.status,
                   w.remarks,
                   w.updated_date,
                   w.order_number
              FROM sug_mai_birds_weighment w
             WHERE TO_CHAR(w.trans_id) = TRIM(?)
                OR TRIM(w.order_number) = TRIM(?)
            """;

    private static final String FIND_BY_IDEMPOTENCY_SQL =
            """
            SELECT w.trans_id,
                   w.status,
                   w.remarks,
                   w.updated_date,
                   w.order_number,
                   TO_CHAR(w.schedule_no) AS schedule_no
              FROM sug_mai_birds_weighment w
             WHERE TRIM(w.order_number) = TRIM(?)
                OR w.remarks LIKE 'IDEMPOTENCY:' || ? || '|%'
            """;

    private static final String SUBMIT_UPDATE_SQL =
            """
            UPDATE sug_mai_birds_weighment w
               SET w.no_of_birds = ?,
                   w.no_of_cages = ?,
                   w.empty_wt = ?,
                   w.gross_wt = ?,
                   w.net_wt = ?,
                   w.avg_wt = ?,
                   w.latitude = ?,
                   w.longitude = ?,
                   w.vehicle_no = NVL(?, w.vehicle_no),
                   w.scale_no = NVL(?, w.scale_no),
                   w.wm_start_time = NVL(?, w.wm_start_time),
                   w.wm_end_time = NVL(?, w.wm_end_time),
                   w.vehicle_outtime = NVL(?, w.vehicle_outtime),
                   w.status = ?,
                   w.npick_status = 'COMPLETED',
                   w.order_number = ?,
                   w.remarks = ?,
                   w.errorcount = 0,
                   w.abortcount = 0,
                   w.updated_date = SYSDATE
             WHERE TO_CHAR(w.schedule_no) = TRIM(?)
               AND """
            + WEIGHMENT_SUPERVISOR_MATCH;

    private static final String RETRY_SQL =
            """
            UPDATE sug_mai_birds_weighment w
               SET w.errorcount = 0,
                   w.abortcount = 0,
                   w.status = 'PENDING',
                   w.remarks = NVL(w.remarks, '') || ' RETRY:' || ?,
                   w.updated_date = SYSDATE
             WHERE TO_CHAR(w.trans_id) = TRIM(?)
                OR TRIM(w.order_number) = TRIM(?)
            """;

    private static final String CORRECTION_SQL =
            """
            UPDATE sug_mai_birds_weighment w
               SET w.remarks = ?,
                   w.updated_date = SYSDATE
             WHERE TO_CHAR(w.trans_id) = TRIM(?)
                OR TRIM(w.order_number) = TRIM(?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public TransactionSqlClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<StoredTransaction> findByIdempotencyKey(String idempotencyKey) {
        List<StoredTransaction> rows = jdbcTemplate.query(
                FIND_BY_IDEMPOTENCY_SQL,
                (rs, rowNum) ->
                        new StoredTransaction(
                                rs.getLong("trans_id"),
                                rs.getString("status"),
                                rs.getString("remarks"),
                                rs.getTimestamp("updated_date"),
                                rs.getString("order_number"),
                                rs.getString("schedule_no")),
                idempotencyKey,
                idempotencyKey);
        return rows.stream().findFirst();
    }

    public SubmitCompletedWeighmentResponse submitCompleted(
            String supervisorCode, SubmitCompletedWeighmentRequest request, String erpStatus) {

        SubmitCompletedWeighmentRequest.WeighmentTotals totals = request.getTotals();
        SubmitCompletedWeighmentRequest.FarmValidation farm = request.getFarmValidation();
        SubmitCompletedWeighmentRequest.CompletionInfo completion = request.getCompletion();
        SubmitCompletedWeighmentRequest.VehicleInfo vehicle = request.getVehicle();
        SubmitCompletedWeighmentRequest.ScaleInfo scale = request.getScale();

        String remarks = formatRemarks(request.getIdempotencyKey(), completion);
        Timestamp wmStart = toTimestamp(completion == null ? null : completion.getWeighingStartTime());
        Timestamp wmEnd = toTimestamp(completion == null ? null : completion.getOutTime());
        Timestamp vehicleOut = wmEnd;

        int updated = jdbcTemplate.update(
                SUBMIT_UPDATE_SQL,
                totals.getTotalBirds(),
                totals.getTotalCagesOrBatches(),
                totals.getTotalTare(),
                totals.getTotalGross(),
                totals.getTotalNet(),
                totals.getOverallAverageBirdWeight(),
                farm.getCapturedLatitude(),
                farm.getCapturedLongitude(),
                vehicle == null ? null : vehicle.getRegistrationNumber(),
                scale == null ? null : scale.getDeviceId(),
                wmStart,
                wmEnd,
                vehicleOut,
                erpStatus,
                request.getLocalTransactionId(),
                remarks,
                request.getScheduleId(),
                supervisorCode,
                supervisorCode);

        if (updated == 0) {
            throw new ResourceNotFoundException(
                    "Weighment header not found for schedule "
                            + request.getScheduleId()
                            + ". Complete download/start flow before submitting.");
        }

        StoredTransaction stored = findByIdempotencyKey(request.getIdempotencyKey()).orElseThrow();
        return toSubmitResponse(stored, request.getLocalTransactionId());
    }

    public TransactionAcknowledgementResponse getAcknowledgement(String transactionId) {
        try {
            return jdbcTemplate.queryForObject(
                    ACKNOWLEDGEMENT_SQL, (rs, rowNum) -> mapAcknowledgement(rs), transactionId, transactionId);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Transaction", transactionId);
        }
    }

    public void retryTransaction(String transactionId, TransactionRetryRequest request) {
        String reason = request.getRetryReason() == null ? "MANUAL_RETRY" : request.getRetryReason();
        int updated = jdbcTemplate.update(RETRY_SQL, reason, transactionId, transactionId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Transaction", transactionId);
        }
    }

    public void submitCorrection(String transactionId, TransactionCorrectionRequest request) {
        String remarks = request.getCorrections() == null
                ? request.getReason()
                : request.getCorrections().getRemarks();
        int updated = jdbcTemplate.update(CORRECTION_SQL, remarks, transactionId, transactionId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Transaction", transactionId);
        }
    }

    static String formatRemarks(String idempotencyKey, SubmitCompletedWeighmentRequest.CompletionInfo completion) {
        String userRemarks =
                completion == null || completion.getRemarks() == null ? "" : completion.getRemarks().trim();
        String prefix = "IDEMPOTENCY:" + idempotencyKey + "|";
        String combined = prefix + userRemarks;
        return combined.length() <= 4000 ? combined : combined.substring(0, 4000);
    }

    static String erpReference(long transId, String orderNumber) {
        if (orderNumber != null && !orderNumber.isBlank()) {
            return orderNumber;
        }
        return "ERP-LIFT-" + transId;
    }

    static String formatInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return Instant.now().atOffset(ZoneOffset.UTC).format(ISO_INSTANT);
        }
        return timestamp.toInstant().atOffset(ZoneOffset.UTC).format(ISO_INSTANT);
    }

    private SubmitCompletedWeighmentResponse toSubmitResponse(
            StoredTransaction stored, String localTransactionId) {
        String status = normalizeStatus(stored.status());
        return SubmitCompletedWeighmentResponse.builder()
                .success(true)
                .status(status)
                .localTransactionId(localTransactionId)
                .erpTransactionReference(erpReference(stored.transId(), stored.orderNumber()))
                .acknowledgedAt(formatInstant(stored.updatedDate()))
                .build();
    }

    private TransactionAcknowledgementResponse mapAcknowledgement(java.sql.ResultSet rs)
            throws java.sql.SQLException {
        long transId = rs.getLong("trans_id");
        String orderNumber = rs.getString("order_number");
        return TransactionAcknowledgementResponse.builder()
                .success(true)
                .transactionId(String.valueOf(transId))
                .status(normalizeStatus(rs.getString("status")))
                .erpTransactionReference(erpReference(transId, orderNumber))
                .message(rs.getString("remarks"))
                .acknowledgedAt(formatInstant(rs.getTimestamp("updated_date")))
                .build();
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "PENDING";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    public record StoredTransaction(
            long transId,
            String status,
            String remarks,
            Timestamp updatedDate,
            String orderNumber,
            String scheduleNo) {

        public SubmitCompletedWeighmentResponse toResponse(String localTransactionId) {
            String normalized =
                    status == null || status.isBlank() ? "PENDING" : status.trim().toUpperCase(Locale.ROOT);
            return SubmitCompletedWeighmentResponse.builder()
                    .success(true)
                    .status(normalized)
                    .localTransactionId(localTransactionId)
                    .erpTransactionReference(erpReference(transId, orderNumber))
                    .acknowledgedAt(formatInstant(updatedDate))
                    .build();
        }
    }
}
