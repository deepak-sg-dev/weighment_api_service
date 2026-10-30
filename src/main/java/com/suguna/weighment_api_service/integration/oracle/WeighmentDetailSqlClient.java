package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.dto.delivery.SubmitDeliveryWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest;
import com.suguna.weighment_api_service.dto.transaction.SubmitCompletedWeighmentRequest.WeighmentEntry;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import com.suguna.weighment_api_service.util.WeighmentSubmitKeys;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class WeighmentDetailSqlClient {

    static final String TXN_PHASE_LIFTING_COMPLETE = "LIFTING_COMPLETE";
    static final String TXN_PHASE_DELIVERY_COMPLETE = "DELIVERY_COMPLETE";
    static final String HEADER_REF_WEIGHMENT_TRANS = "WEIGHMENT_TRANS";
    static final String HEADER_REF_RETAIL_ORDER = "RETAIL_ORDER";
    static final int STATUS_ACTIVE = 1;
    static final int SUBMIT_BATCH_NO = 1;

    private static final String RESOLVE_TRANS_ID_SQL =
            """
            SELECT w.trans_id
              FROM sug_mai_birds_weighment w
             WHERE """
            + ScheduleIdSupport.WEIGHMENT_SCHEDULE_NO_MATCH
            + """
             ORDER BY w.updated_date DESC NULLS LAST
             FETCH FIRST 1 ROW ONLY
            """;

    private static final String RESOLVE_LIFTING_SOURCE_SQL =
            """
            SELECT NVL(o.source, 'LIFTING') AS order_source
              FROM sug_mai_birds_lifting_order o
             WHERE """
            + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH;

    private static final String COUNT_DTL_SQL =
            """
            SELECT COUNT(*)
              FROM sug_mai_birds_weighment_dtl d
             WHERE d.local_transaction_id = TRIM(?)
               AND d.txn_phase = ?
               AND d.submit_batch_no = ?
            """;

    private static final String LIST_DTL_SQL =
            """
            SELECT TO_CHAR(d.dtl_id) AS dtl_id,
                   d.line_sequence,
                   d.cage_or_set_ref AS category,
                   d.birds_applied,
                   d.tare_wt,
                   d.gross_wt,
                   d.net_wt,
                   d.avg_bird_wt,
                   d.tolerance_status,
                   d.row_status,
                   d.captured_at,
                   d.event_at,
                   d.capture_lat,
                   d.capture_long,
                   d.capture_accuracy_m,
                   d.scale_no,
                   d.device_id,
                   d.ext_json,
                   d.creation_date
              FROM sug_mai_birds_weighment_dtl d
             WHERE d.local_transaction_id = TRIM(?)
               AND d.txn_phase = ?
               AND d.submit_batch_no = ?
             ORDER BY d.line_sequence ASC, d.dtl_id ASC
            """;

    private static final String INSERT_DTL_SQL =
            """
            INSERT INTO sug_mai_birds_weighment_dtl (
                dtl_id,
                business_module,
                txn_phase,
                header_ref_type,
                header_ref_id,
                schedule_no,
                trans_id,
                retail_order_no,
                retailer_code,
                wm_supervisor_code,
                local_transaction_id,
                submit_batch_no,
                line_sequence,
                cage_or_set_ref,
                birds_applied,
                birds_adjusted,
                adjustment_reason,
                tare_wt,
                gross_wt,
                net_wt,
                avg_bird_wt,
                tolerance_status,
                captured_at,
                event_at,
                capture_lat,
                capture_long,
                capture_accuracy_m,
                scale_no,
                device_id,
                row_status,
                ext_json,
                status,
                created_by,
                creation_date
            ) VALUES (
                sug_mai_birds_weighment_dtl_seq.NEXTVAL,
                ?, ?, ?, ?, ?, ?,
                ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                SYSDATE
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public WeighmentDetailSqlClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasDetailLines(String scheduleTransactionKey) {
        return hasDetailLinesForPhase(scheduleTransactionKey, TXN_PHASE_LIFTING_COMPLETE);
    }

    public boolean hasDeliveryDetailLines(String deliveryTransactionKey) {
        return hasDetailLinesForPhase(deliveryTransactionKey, TXN_PHASE_DELIVERY_COMPLETE);
    }

    private boolean hasDetailLinesForPhase(String localTransactionId, String txnPhase) {
        Integer count = jdbcTemplate.queryForObject(
                COUNT_DTL_SQL,
                Integer.class,
                localTransactionId,
                txnPhase,
                SUBMIT_BATCH_NO);
        return count != null && count > 0;
    }

    public List<StoredWeighmentDetailLine> listLiftingCompleteLines(String scheduleTransactionKey) {
        return jdbcTemplate.query(
                LIST_DTL_SQL,
                (rs, rowNum) ->
                        new StoredWeighmentDetailLine(
                                rs.getString("dtl_id"),
                                getInteger(rs, "line_sequence"),
                                rs.getString("category"),
                                getInteger(rs, "birds_applied"),
                                getDouble(rs, "tare_wt"),
                                getDouble(rs, "gross_wt"),
                                getDouble(rs, "net_wt"),
                                getDouble(rs, "avg_bird_wt"),
                                parseWeightDifference(rs.getString("tolerance_status")),
                                rs.getString("row_status"),
                                rs.getTimestamp("captured_at"),
                                rs.getTimestamp("event_at"),
                                getDouble(rs, "capture_lat"),
                                getDouble(rs, "capture_long"),
                                getDouble(rs, "capture_accuracy_m"),
                                rs.getString("scale_no"),
                                rs.getString("device_id"),
                                rs.getString("ext_json"),
                                rs.getTimestamp("creation_date")),
                scheduleTransactionKey,
                TXN_PHASE_LIFTING_COMPLETE,
                SUBMIT_BATCH_NO);
    }

    public List<StoredWeighmentDetailLine> listDeliveryCompleteLines(String deliveryTransactionKey) {
        return jdbcTemplate.query(
                LIST_DTL_SQL,
                (rs, rowNum) ->
                        new StoredWeighmentDetailLine(
                                rs.getString("dtl_id"),
                                getInteger(rs, "line_sequence"),
                                rs.getString("category"),
                                getInteger(rs, "birds_applied"),
                                getDouble(rs, "tare_wt"),
                                getDouble(rs, "gross_wt"),
                                getDouble(rs, "net_wt"),
                                getDouble(rs, "avg_bird_wt"),
                                parseWeightDifference(rs.getString("tolerance_status")),
                                rs.getString("row_status"),
                                rs.getTimestamp("captured_at"),
                                rs.getTimestamp("event_at"),
                                getDouble(rs, "capture_lat"),
                                getDouble(rs, "capture_long"),
                                getDouble(rs, "capture_accuracy_m"),
                                rs.getString("scale_no"),
                                rs.getString("device_id"),
                                rs.getString("ext_json"),
                                rs.getTimestamp("creation_date")),
                deliveryTransactionKey,
                TXN_PHASE_DELIVERY_COMPLETE,
                SUBMIT_BATCH_NO);
    }

    public Optional<Long> resolveTransId(String scheduleId, String supervisorCode) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        List<Long> ids = jdbcTemplate.query(
                RESOLVE_TRANS_ID_SQL,
                (rs, rowNum) -> rs.getLong("trans_id"),
                normalizedScheduleId,
                normalizedScheduleId);
        return ids.stream().findFirst();
    }

    public void insertWeighmentDetails(
            String supervisorCode,
            String scheduleId,
            long transId,
            String businessModule,
            String scheduleTransactionKey,
            SubmitCompletedWeighmentRequest request) {

        if (hasDetailLines(scheduleTransactionKey)) {
            return;
        }

        SubmitCompletedWeighmentRequest.FarmValidation farm = request.getFarmValidation();
        SubmitCompletedWeighmentRequest.ScaleInfo scale = request.getScale();
        String scaleDeviceId = scale == null ? null : scale.getDeviceId();
        Double captureLat = farm == null ? null : farm.getCapturedLatitude();
        Double captureLong = farm == null ? null : farm.getCapturedLongitude();
        Double accuracy = farm == null ? null : farm.getAccuracyMeters();

        Long scheduleNo = ScheduleIdSupport.parseErpIndentAsLong(scheduleId);
        String headerRefId = String.valueOf(transId);

        List<Object[]> batch = new ArrayList<>();
        List<WeighmentEntry> entries = request.getWeighmentEntries();
        for (int i = 0; i < entries.size(); i++) {
            WeighmentEntry entry = entries.get(i);
            String category = normalizeCategory(entry.getCategory());
            boolean adjusted = !"NORMAL".equals(category);
            int lineSequence = entry.getSequence() != null ? entry.getSequence() : i + 1;
            String extJson = truncateExtJson(
                    WeighmentDtlHistoryJson.entryJson(request, entry, i == 0));
            batch.add(new Object[] {
                mapBusinessModule(businessModule),
                TXN_PHASE_LIFTING_COMPLETE,
                HEADER_REF_WEIGHMENT_TRANS,
                headerRefId,
                scheduleNo,
                transId,
                null,
                null,
                supervisorCode,
                scheduleTransactionKey,
                SUBMIT_BATCH_NO,
                lineSequence,
                category,
                entry.getBirdsApplied(),
                adjusted ? "Y" : "N",
                adjusted ? category : null,
                deriveTare(entry),
                entry.getGross(),
                entry.getNet(),
                entry.getAverageBirdWeight(),
                entry.getWeightDifference() == null ? null : String.valueOf(entry.getWeightDifference()),
                toTimestamp(entry.getStartTime()),
                toTimestamp(entry.getEndTime()),
                captureLat,
                captureLong,
                accuracy,
                scaleDeviceId,
                request.getDeviceId(),
                mapRowStatus(entry.getStatus()),
                extJson,
                STATUS_ACTIVE,
                null
            });
        }

        jdbcTemplate.batchUpdate(INSERT_DTL_SQL, batch);
    }

    /** {@code EXT_JSON} is CLOB-capable but keep a safe cap for VARCHAR2-backed columns. */
    private static String truncateExtJson(String json) {
        if (json == null) {
            return null;
        }
        return json.length() <= 3900 ? json : json.substring(0, 3900);
    }

    public void insertDeliveryWeighmentDetails(
            String supervisorCode,
            String scheduleId,
            long transId,
            String retailOrderNo,
            String retailerCode,
            String deliveryTransactionKey,
            SubmitDeliveryWeighmentRequest request) {

        if (hasDeliveryDetailLines(deliveryTransactionKey)) {
            return;
        }

        List<WeighmentEntry> entries = request.getWeighmentEntries();
        if (entries == null || entries.isEmpty()) {
            throw new IllegalStateException("Delivery weighment requires at least one weighmentEntries row");
        }

        SubmitDeliveryWeighmentRequest.ShopValidation shop = request.getShopValidation();
        SubmitCompletedWeighmentRequest.ScaleInfo scale = request.getScale();
        String scaleDeviceId = scale == null ? null : scale.getDeviceId();
        Double captureLat = shop == null ? null : shop.getLatitude();
        Double captureLong = shop == null ? null : shop.getLongitude();

        Long scheduleNo = ScheduleIdSupport.parseErpIndentAsLong(scheduleId);
        String headerRefId = retailOrderNo == null ? "" : retailOrderNo.trim();
        String retailer = retailerCode == null ? null : retailerCode.trim();
        Long retailOrderNumeric = parseRetailOrderNo(retailOrderNo);

        List<Object[]> batch = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            WeighmentEntry entry = entries.get(i);
            String category = normalizeCategory(entry.getCategory());
            boolean adjusted = !"NORMAL".equals(category);
            batch.add(new Object[] {
                "RETAIL",
                TXN_PHASE_DELIVERY_COMPLETE,
                HEADER_REF_RETAIL_ORDER,
                headerRefId,
                scheduleNo,
                transId,
                retailOrderNumeric,
                retailer,
                supervisorCode,
                deliveryTransactionKey,
                SUBMIT_BATCH_NO,
                entry.getSequence(),
                category,
                entry.getBirdsApplied(),
                adjusted ? "Y" : "N",
                adjusted ? category : null,
                deriveTare(entry),
                entry.getGross(),
                entry.getNet(),
                entry.getAverageBirdWeight(),
                entry.getWeightDifference() == null ? null : String.valueOf(entry.getWeightDifference()),
                toTimestamp(entry.getStartTime()),
                toTimestamp(entry.getEndTime()),
                captureLat,
                captureLong,
                null,
                scaleDeviceId,
                request.getDeviceId(),
                mapRowStatus(entry.getStatus()),
                WeighmentDtlHistoryJson.deliveryEntryJson(request, entry, i == 0),
                STATUS_ACTIVE,
                null
            });
        }

        jdbcTemplate.batchUpdate(INSERT_DTL_SQL, batch);
    }

    private static Long parseRetailOrderNo(String retailOrderNo) {
        if (retailOrderNo == null || retailOrderNo.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(retailOrderNo.trim().replaceAll("\\D", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String resolveBusinessModule(String scheduleId, String saleType, String birdType) {
        if (saleType != null && !saleType.isBlank()) {
            String normalized = saleType.trim().toUpperCase(Locale.ROOT);
            if ("RETAIL".equals(normalized)) {
                return "RETAIL";
            }
            if ("WHOLESALE".equals(normalized)) {
                return "WHOLESALE";
            }
        }
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        List<String> sources = jdbcTemplate.query(
                RESOLVE_LIFTING_SOURCE_SQL,
                (rs, rowNum) -> rs.getString("order_source"),
                normalizedScheduleId,
                normalizedScheduleId);
        if (!sources.isEmpty() && sources.get(0) != null) {
            String source = sources.get(0).trim().toUpperCase(Locale.ROOT);
            if ("RETAIL".equals(source)) {
                return "RETAIL";
            }
        }
        if (birdType != null) {
            String op = birdType.trim().toUpperCase(Locale.ROOT);
            if (op.contains("CULL")) {
                return "CULL";
            }
            if (op.contains("WHOLE")) {
                return "WHOLESALE";
            }
        }
        return "LIFTING";
    }

    private static String mapBusinessModule(String module) {
        return module == null || module.isBlank() ? "LIFTING" : module.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "NORMAL";
        }
        return category.trim().toUpperCase(Locale.ROOT);
    }

    private static String mapRowStatus(String status) {
        if (status != null && "ABORTED".equalsIgnoreCase(status.trim())) {
            return "ABORTED";
        }
        return "ACCEPTED";
    }

    private static Double deriveTare(WeighmentEntry entry) {
        if (entry.getGross() == null || entry.getNet() == null) {
            return null;
        }
        return entry.getGross() - entry.getNet();
    }

    private static Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Double getDouble(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static Integer getInteger(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static Double parseWeightDifference(String toleranceStatus) {
        if (toleranceStatus == null || toleranceStatus.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(toleranceStatus.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record StoredWeighmentDetailLine(
            String dtlId,
            Integer lineSequence,
            String category,
            Integer birdsApplied,
            Double tareWt,
            Double grossWt,
            Double netWt,
            Double avgBirdWt,
            Double weightDifference,
            String rowStatus,
            Timestamp capturedAt,
            Timestamp eventAt,
            Double captureLat,
            Double captureLong,
            Double captureAccuracyM,
            String scaleNo,
            String deviceId,
            String extJson,
            Timestamp creationDate) {}
}
