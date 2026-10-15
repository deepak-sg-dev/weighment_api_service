package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionAcknowledgementResponse;
import com.suguna.weighment_api_service.dto.transaction.TransactionCorrectionRequest;
import com.suguna.weighment_api_service.dto.transaction.TransactionRetryRequest;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import com.suguna.weighment_api_service.util.WeighmentSubmitKeys;
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
    private static final String RECEIVED_STATUS = "RECEIVED";

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
                   w.manual_dc_no
              FROM sug_mai_birds_weighment w
             WHERE TO_CHAR(w.trans_id) = TRIM(?)
                OR TRIM(w.manual_dc_no) = TRIM(?)
            """;

    private static final String FIND_BY_SCHEDULE_SQL =
            """
            SELECT w.trans_id,
                   w.status,
                   w.remarks,
                   w.updated_date,
                   w.manual_dc_no,
                   TO_CHAR(w.schedule_no) AS schedule_no
              FROM sug_mai_birds_weighment w
             WHERE """
            + ScheduleIdSupport.WEIGHMENT_SCHEDULE_NO_MATCH
            + """
               AND """
            + WEIGHMENT_SUPERVISOR_MATCH
            + """
               AND (w.npick_status = 'COMPLETED'
                    OR UPPER(NVL(w.status, 'X')) IN ('RECEIVED', 'ACCEPTED', 'PENDING'))
             ORDER BY w.updated_date DESC NULLS LAST
             FETCH FIRST 1 ROW ONLY
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
                   w.manual_dc_no = ?,
                   w.remarks = ?,
                   w.errorcount = 0,
                   w.abortcount = 0,
                   w.updated_date = SYSDATE
             WHERE """
            + ScheduleIdSupport.WEIGHMENT_SCHEDULE_NO_MATCH
            + """
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
                OR TRIM(w.manual_dc_no) = TRIM(?)
            """;

    private static final String CORRECTION_SQL =
            """
            UPDATE sug_mai_birds_weighment w
               SET w.remarks = ?,
                   w.updated_date = SYSDATE
             WHERE TO_CHAR(w.trans_id) = TRIM(?)
                OR TRIM(w.manual_dc_no) = TRIM(?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final WeighmentDetailSqlClient weighmentDetailSqlClient;

    public TransactionSqlClient(JdbcTemplate jdbcTemplate, WeighmentDetailSqlClient weighmentDetailSqlClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.weighmentDetailSqlClient = weighmentDetailSqlClient;
    }

    public Optional<StoredTransaction> findByScheduleId(String scheduleId, String supervisorCode) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        List<StoredTransaction> rows = jdbcTemplate.query(
                FIND_BY_SCHEDULE_SQL,
                (rs, rowNum) ->
                        new StoredTransaction(
                                rs.getLong("trans_id"),
                                rs.getString("status"),
                                rs.getString("remarks"),
                                rs.getTimestamp("updated_date"),
                                rs.getString("manual_dc_no"),
                                rs.getString("schedule_no")),
                normalizedScheduleId,
                normalizedScheduleId,
                supervisorCode,
                supervisorCode);
        return rows.stream().findFirst();
    }

    public boolean hasWeighmentDetailLines(String scheduleTransactionKey) {
        return weighmentDetailSqlClient.hasDetailLines(scheduleTransactionKey);
    }

    public SubmitCompletedWeighmentResponse submitCompleted(
            String supervisorCode, SubmitCompletedWeighmentRequest request, String erpStatus) {

        SubmitCompletedWeighmentRequest.WeighmentTotals totals = request.getTotals();
        SubmitCompletedWeighmentRequest.FarmValidation farm = request.getFarmValidation();
        SubmitCompletedWeighmentRequest.CompletionInfo completion = request.getCompletion();
        SubmitCompletedWeighmentRequest.VehicleInfo vehicle = request.getVehicle();
        SubmitCompletedWeighmentRequest.ScaleInfo scale = request.getScale();

        String normalizedScheduleId = ScheduleIdSupport.normalize(request.getScheduleId());
        String transactionKey = WeighmentSubmitKeys.transactionKey(normalizedScheduleId);
        String remarks = formatRemarks(normalizedScheduleId, request, completion);
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
                normalizedScheduleId,
                remarks,
                normalizedScheduleId,
                normalizedScheduleId,
                supervisorCode,
                supervisorCode);

        if (updated == 0) {
            throw new ResourceNotFoundException(
                    "Weighment header not found for schedule "
                            + request.getScheduleId()
                            + ". Complete download/start flow before submitting.");
        }

        long transId = weighmentDetailSqlClient
                .resolveTransId(request.getScheduleId(), supervisorCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Weighment header trans_id not found for schedule " + request.getScheduleId()));

        String businessModule = weighmentDetailSqlClient.resolveBusinessModule(
                request.getScheduleId(), request.effectiveSaleType(), request.effectiveBirdType());
        weighmentDetailSqlClient.insertWeighmentDetails(
                supervisorCode,
                request.getScheduleId(),
                transId,
                businessModule,
                transactionKey,
                request);

        StoredTransaction stored = findByScheduleId(normalizedScheduleId, supervisorCode).orElseThrow();
        return toSubmitResponse(stored, normalizedScheduleId);
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

    static String formatRemarks(
            String scheduleId, SubmitCompletedWeighmentRequest request, SubmitCompletedWeighmentRequest.CompletionInfo completion) {
        String userRemarks =
                completion == null || completion.getRemarks() == null ? "" : completion.getRemarks().trim();
        String saleType = request.effectiveSaleType() == null ? "" : request.effectiveSaleType();
        String birdType = request.effectiveBirdType() == null ? "" : request.effectiveBirdType();
        String prefix = "SUBMIT:" + scheduleId + "|saleType=" + saleType + "|birdType=" + birdType + "|";
        String combined = prefix + userRemarks;
        return combined.length() <= 4000 ? combined : combined.substring(0, 4000);
    }

    static String formatInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return Instant.now().atOffset(ZoneOffset.UTC).format(ISO_INSTANT);
        }
        return timestamp.toInstant().atOffset(ZoneOffset.UTC).format(ISO_INSTANT);
    }

    private SubmitCompletedWeighmentResponse toSubmitResponse(StoredTransaction stored, String scheduleId) {
        return SubmitCompletedWeighmentResponse.builder()
                .success(true)
                .message("Weighment received")
                .scheduleId(scheduleId)
                .status(RECEIVED_STATUS)
                .receivedAt(formatInstant(stored.updatedDate()))
                .build();
    }

    private TransactionAcknowledgementResponse mapAcknowledgement(java.sql.ResultSet rs)
            throws java.sql.SQLException {
        long transId = rs.getLong("trans_id");
        String manualDcNo = rs.getString("manual_dc_no");
        return TransactionAcknowledgementResponse.builder()
                .success(true)
                .transactionId(String.valueOf(transId))
                .status(normalizeStatus(rs.getString("status")))
                .erpTransactionReference(manualDcNo != null ? manualDcNo : "ERP-LIFT-" + transId)
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
            String manualDcNo,
            String scheduleNo) {

        public SubmitCompletedWeighmentResponse toResponse(String scheduleId) {
            return SubmitCompletedWeighmentResponse.builder()
                    .success(true)
                    .message("Weighment received")
                    .scheduleId(scheduleId)
                    .status(RECEIVED_STATUS)
                    .receivedAt(formatInstant(updatedDate))
                    .build();
        }
    }
}
