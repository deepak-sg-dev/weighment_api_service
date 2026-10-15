package com.suguna.weighment_api_service.integration.oracle;

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
    static final String HEADER_REF_WEIGHMENT_TRANS = "WEIGHMENT_TRANS";
    static final int STATUS_ACTIVE = 1;
    static final int SUBMIT_BATCH_NO = 1;

    private static final String WEIGHMENT_SUPERVISOR_MATCH =
            """
             (TRIM(w.wm_supervisor_code) = TRIM(?)
              OR LTRIM(TRIM(w.wm_supervisor_code), '0') = LTRIM(TRIM(?), '0'))
            """;

    private static final String RESOLVE_TRANS_ID_SQL =
            """
            SELECT w.trans_id
              FROM sug_mai_birds_weighment w
             WHERE """
            + ScheduleIdSupport.WEIGHMENT_SCHEDULE_NO_MATCH
            + """
               AND """
            + WEIGHMENT_SUPERVISOR_MATCH;

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
                ?, ?, ?, ?, ?, ?, NULL, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public WeighmentDetailSqlClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasDetailLines(String scheduleTransactionKey) {
        Integer count = jdbcTemplate.queryForObject(
                COUNT_DTL_SQL,
                Integer.class,
                scheduleTransactionKey,
                TXN_PHASE_LIFTING_COMPLETE,
                SUBMIT_BATCH_NO);
        return count != null && count > 0;
    }

    public Optional<Long> resolveTransId(String scheduleId, String supervisorCode) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        List<Long> ids = jdbcTemplate.query(
                RESOLVE_TRANS_ID_SQL,
                (rs, rowNum) -> rs.getLong("trans_id"),
                normalizedScheduleId,
                normalizedScheduleId,
                supervisorCode,
                supervisorCode);
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
            batch.add(new Object[] {
                mapBusinessModule(businessModule),
                TXN_PHASE_LIFTING_COMPLETE,
                HEADER_REF_WEIGHMENT_TRANS,
                headerRefId,
                scheduleNo,
                transId,
                supervisorCode,
                scheduleTransactionKey,
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
                accuracy,
                scaleDeviceId,
                request.getDeviceId(),
                mapRowStatus(entry.getStatus()),
                WeighmentDtlHistoryJson.entryJson(request, entry, i == 0),
                STATUS_ACTIVE,
                null
            });
        }

        jdbcTemplate.batchUpdate(INSERT_DTL_SQL, batch);
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
}
