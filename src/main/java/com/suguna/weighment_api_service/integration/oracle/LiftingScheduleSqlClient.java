package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.config.OracleLiftingProperties;
import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.MarkScheduleDownloadedRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.StartScheduleRequest;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class LiftingScheduleSqlClient {

    private static final String WEIGHMENT_SUPERVISOR_MATCH =
            """
             (TRIM(w.wm_supervisor_code) = TRIM(?)
              OR LTRIM(TRIM(w.wm_supervisor_code), '0') = LTRIM(TRIM(?), '0'))
            """;

    private static final String WEIGHMENT_JOIN_ON_SCHEDULE =
            """
             (TO_CHAR(w.schedule_no) = TO_CHAR(o.erpindentid)
              OR TO_CHAR(w.schedule_no) = REGEXP_SUBSTR(TO_CHAR(o.erpindentid), '^[^/]+'))
            """;

    /** List: no GPS view join; status and date filters applied in SQL. */
    private static final String LIST_SCHEDULES_SELECT =
            """
            SELECT TO_CHAR(o.erpindentid) AS schedule_id,
                   o.mobileuserempid AS supervisor_code,
                   f.farm_code AS farm_id,
                   f.farm_name_village AS farm_name,
                   NVL(o.breed, NVL(o.ordertype, 'BROILER')) AS operation_type,
                   NVL(o.weighment_type, 'HANGING') AS scale_type,
                   NVL(o.no_of_birds, 0) AS planned_quantity,
                   NVL(o.no_of_kgs, 0) AS planned_quantity_kg,
                   o.order_request_date,
                   o.branchcode AS branch_code,
                   TO_CHAR(o.branchid) AS branch_id,
                   o.ordernumber AS sales_order_no,
                   NVL(o.body_wt, 0) AS body_wt,
                   o.vehicleno AS vehicle_no,
                   NVL(f.farm_name_village, o.farmername) AS farm_location,
                   NVL(o.posted_flag, 'N') AS posted_flag,
                   NVL(w.npick_status, 'OPEN') AS npick_status,
                   o.source AS order_source
              FROM sug_mai_birds_lifting_order o
              LEFT JOIN sug_mai_birds_weighment w
                ON """
            + WEIGHMENT_JOIN_ON_SCHEDULE
            + """
               AND w.wm_supervisor_code = o.mobileuserempid
              LEFT JOIN sug_farm_mst_header f
                ON TRIM(f.farm_code) = TRIM(o.farmercode)
            """;

    /** Detail: includes GPS coordinates and full party/product fields. */
    private static final String DETAIL_SCHEDULES_SELECT =
            """
            SELECT TO_CHAR(o.erpindentid) AS schedule_id,
                   o.mobileuserempid AS supervisor_code,
                   f.farm_code AS farm_id,
                   f.farm_name_village AS farm_name,
                   NVL(o.breed, NVL(o.ordertype, 'BROILER')) AS operation_type,
                   NVL(o.weighment_type, 'HANGING') AS scale_type,
                   NVL(o.no_of_birds, 0) AS planned_quantity,
                   NVL(o.no_of_kgs, 0) AS planned_quantity_kg,
                   o.order_request_date,
                   o.branchcode AS branch_code,
                   TO_CHAR(o.branchid) AS branch_id,
                   o.ordernumber AS sales_order_no,
                   NVL(o.body_wt, 0) AS body_wt,
                   o.vehicleno AS vehicle_no,
                   NVL(f.farm_name_village, o.farmername) AS farm_location,
                   NVL(o.posted_flag, 'N') AS posted_flag,
                   NVL(w.npick_status, 'OPEN') AS npick_status,
                   o.source AS order_source,
                   NVL(v.farm_mst_latitude, 0) AS farm_latitude,
                   NVL(v.farm_mst_longitude, 0) AS farm_longitude,
                   NVL(o.tolerancepercent, 0) AS tolerance_percent,
                   NVL(o.avg_wt_low, 0) AS tolerance_min,
                   NVL(o.avg_wt_high, 0) AS tolerance_max,
                   NVL(o.body_wt, 0) AS body_wt,
                   o.farmercode AS farmer_id,
                   f.farmer_name AS farmer_name,
                   o.farmer_mobile_no AS farmer_mobile,
                   o.customercode AS trader_id,
                   o.cus_name AS trader_name,
                   NVL(o.breed, 'BROILER') AS product_id,
                   NVL(o.breed, 'Broiler') AS product_name
              FROM sug_mai_birds_lifting_order o
              LEFT JOIN sug_mai_birds_weighment w
                ON """
            + WEIGHMENT_JOIN_ON_SCHEDULE
            + """
               AND w.wm_supervisor_code = o.mobileuserempid
              LEFT JOIN sug_farm_mst_header f
                ON TRIM(f.farm_code) = TRIM(o.farmercode)
              LEFT JOIN sug_farm_lifting_gps_v v
                ON v.farm_code = o.farmercode
               AND v.branch_code = o.branchcode
            """;

    private static final String ORDER_SUPERVISOR_MATCH =
            """
             (TRIM(o.mobileuserempid) = TRIM(?)
              OR LTRIM(TRIM(o.mobileuserempid), '0') = LTRIM(TRIM(?), '0'))
            """;

    private static final String MARK_DOWNLOADED_SQL =
            // ERP stores downloaded status as 'Y' in NPICK_STATUS. DOWNLOADED changed to Y
            """
            UPDATE sug_mai_birds_weighment w
               SET w.npick_status = 'DOWNLOADED',  
                   w.updated_date = SYSDATE
             WHERE """
            + ScheduleIdSupport.WEIGHMENT_SCHEDULE_NO_MATCH
            + """
               AND """
            + WEIGHMENT_SUPERVISOR_MATCH;

    private static final String START_SCHEDULE_SQL =
            """
            UPDATE sug_mai_birds_lifting_order o
               SET o.vehicleno = NVL(?, o.vehicleno),
                   o.driver = ?,
                   o.order_request_date = ?
             WHERE """
            + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH
            + """
               AND """
            + ORDER_SUPERVISOR_MATCH;

    private final JdbcTemplate jdbcTemplate;
    private final OracleErpProperties erpProperties;
    private final OracleLiftingProperties liftingProperties;

    public LiftingScheduleSqlClient(
            JdbcTemplate jdbcTemplate,
            OracleErpProperties erpProperties,
            OracleLiftingProperties liftingProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.erpProperties = erpProperties;
        this.liftingProperties = liftingProperties;
    }

    public List<LiftingSchedule> listSchedules(String supervisorCode, Set<ScheduleStatus> statuses) {
        List<String> supervisorKeys = supervisorBindKeys(supervisorCode);
        String sql = LIST_SCHEDULES_SELECT
                + " WHERE "
                + supervisorEqualityPredicate(supervisorKeys)
                + " AND NVL(o.application, ?) = ?"
                + lookbackPredicate()
                + buildStatusWhereClause(statuses);

        List<Object> args = new ArrayList<>();
        args.addAll(supervisorKeys);
        args.add(erpProperties.getApplicationCode());
        args.add(erpProperties.getApplicationCode());
        if (erpProperties.getScheduleListLookbackDays() > 0) {
            args.add(erpProperties.getScheduleListLookbackDays());
        }

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapListSchedule(rs), args.toArray());
    }

    public LiftingSchedule getScheduleDetails(String supervisorCode, String scheduleId) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        String sql = DETAIL_SCHEDULES_SELECT
                + " WHERE "
                + ORDER_SUPERVISOR_MATCH
                + """
                 AND NVL(o.application, ?) = ?
                 AND """
                + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH;
        List<LiftingSchedule> schedules = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapDetailSchedule(rs),
                supervisorCode,
                supervisorCode,
                erpProperties.getApplicationCode(),
                erpProperties.getApplicationCode(),
                normalizedScheduleId,
                normalizedScheduleId);
        if (schedules.isEmpty()) {
            return null;
        }
        return schedules.get(0);
    }

    public void markScheduleDownloaded(String supervisorCode, String scheduleId, MarkScheduleDownloadedRequest request) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        int weighmentUpdated = jdbcTemplate.update(
                MARK_DOWNLOADED_SQL,
                normalizedScheduleId,
                normalizedScheduleId,
                supervisorCode,
                supervisorCode);
        String orderSql =
                """
                UPDATE sug_mai_birds_lifting_order o
                   SET o.source = 'MOBILE_DOWNLOADED',
                       o.order_request_date = NVL(o.order_request_date, ?)
                 WHERE """
                + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH
                + """
                   AND """
                + ORDER_SUPERVISOR_MATCH;
        int orderUpdated = jdbcTemplate.update(
                orderSql,
                Timestamp.from(request.getDownloadedAt()),
                normalizedScheduleId,
                normalizedScheduleId,
                supervisorCode,
                supervisorCode);
        if (weighmentUpdated == 0 && orderUpdated == 0) {
            throw new ResourceNotFoundException("Schedule", scheduleId);
        }
    }

    public void startSchedule(String supervisorCode, String scheduleId, StartScheduleRequest request) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        int updated = jdbcTemplate.update(
                START_SCHEDULE_SQL,
                request.getVehicleRegistration(),
                request.getLocalTransactionId(),
                Timestamp.from(request.getStartedAt()),
                normalizedScheduleId,
                normalizedScheduleId,
                supervisorCode,
                supervisorCode);
        if (updated == 0) {
            throw new ResourceNotFoundException("Schedule", scheduleId);
        }
        invokeOrderStatusValidation(scheduleId);
    }

    /** Optional ERP function from {@code sug_mai_birds_lifting_pkg.get_order_status}. */
    public void invokeOrderStatusValidation(String scheduleId) {
        try {
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                    .withSchemaName(liftingProperties.getSchemaName())
                    .withCatalogName(liftingProperties.getPackageName())
                    .withFunctionName("get_order_status");
            Map<String, Object> in = new HashMap<>();
            in.put("p_line_id", ScheduleIdSupport.parseErpIndentAsLong(scheduleId));
            call.execute(in);
        } catch (RuntimeException ex) {
            // Non-blocking until ERP confirms line-id mapping.
        }
    }

    static List<String> supervisorBindKeys(String supervisorCode) {
        String trimmed = supervisorCode == null ? "" : supervisorCode.trim();
        if (trimmed.isEmpty()) {
            return List.of("");
        }
        String withoutLeadingZeros = trimmed.replaceFirst("^0+(?!$)", "");
        if (withoutLeadingZeros.equals(trimmed)) {
            return List.of(trimmed);
        }
        return List.of(trimmed, withoutLeadingZeros);
    }

    private static String supervisorEqualityPredicate(List<String> keys) {
        if (keys.size() == 1) {
            return "o.mobileuserempid = ?";
        }
        return "(o.mobileuserempid = ? OR o.mobileuserempid = ?)";
    }

    private String lookbackPredicate() {
        if (erpProperties.getScheduleListLookbackDays() <= 0) {
            return "";
        }
        // Open orders stay visible even when order_request_date is old; trim old completed rows only.
        return """
             AND (NVL(o.posted_flag, 'N') <> 'Y'
                  OR o.order_request_date IS NULL
                  OR o.order_request_date >= TRUNC(SYSDATE) - ?)
            """;
    }

    private static String buildStatusWhereClause(Set<ScheduleStatus> statuses) {
        if (statuses.isEmpty()) {
            return " AND NVL(o.posted_flag, 'N') <> 'Y'";
        }
        if (statuses.size() == 1 && statuses.contains(ScheduleStatus.COMPLETED)) {
            return " AND NVL(o.posted_flag, 'N') = 'Y'";
        }

        List<String> parts = new ArrayList<>();
        for (ScheduleStatus status : statuses) {
            parts.add(statusPredicate(status));
        }

        StringBuilder clause = new StringBuilder(" AND (");
        if (!statuses.contains(ScheduleStatus.COMPLETED)) {
            clause.append("NVL(o.posted_flag, 'N') <> 'Y' AND (");
        }
        clause.append(String.join(" OR ", parts));
        clause.append(statuses.contains(ScheduleStatus.COMPLETED) ? ")" : "))");
        return clause.toString();
    }

    private static String statusPredicate(ScheduleStatus status) {
        return switch (status) {
            case ASSIGNED ->
                    """
                    (NVL(o.source, 'X') <> 'MOBILE_DOWNLOADED'
                     AND NVL(w.npick_status, 'OPEN') <> 'DOWNLOADED'
                     AND (w.npick_status IS NULL OR UPPER(w.npick_status) NOT LIKE '%PROGRESS%'))
                    """;
            case DOWNLOADED ->
                    """
                    (NVL(w.npick_status, 'OPEN') = 'DOWNLOADED'
                     OR o.source = 'MOBILE_DOWNLOADED')
                    """;
            case IN_PROGRESS ->
                    "(w.npick_status IS NOT NULL AND UPPER(w.npick_status) LIKE '%PROGRESS%')";
            case COMPLETED -> "NVL(o.posted_flag, 'N') = 'Y'";
            case CLOSED -> "1 = 0";
        };
    }

    private LiftingSchedule mapListSchedule(java.sql.ResultSet rs) throws java.sql.SQLException {
        return mapSchedule(rs, false);
    }

    private LiftingSchedule mapDetailSchedule(java.sql.ResultSet rs) throws java.sql.SQLException {
        return mapSchedule(rs, true);
    }

    private LiftingSchedule mapSchedule(java.sql.ResultSet rs, boolean detail) throws java.sql.SQLException {
        String postedFlag = rs.getString("posted_flag");
        String npickStatus = rs.getString("npick_status");
        String orderSource = rs.getString("order_source");
        ScheduleStatus status = mapStatus(postedFlag, npickStatus, orderSource);
        boolean downloaded = "Y".equalsIgnoreCase(npickStatus)
                || "MOBILE_DOWNLOADED".equalsIgnoreCase(orderSource);

        LiftingSchedule.LiftingScheduleBuilder builder = LiftingSchedule.builder()
                .scheduleId(rs.getString("schedule_id"))
                .supervisorId(rs.getString("supervisor_code"))
                .orderSource(rs.getString("order_source"))
                .scheduledAt(readInstant(rs, "order_request_date"))
                .plannedQuantityKg(rs.getDouble("planned_quantity_kg"))
                .declaredBodyWeightKg(rs.getDouble("body_wt"))
                .branchCode(rs.getString("branch_code"))
                .branchId(rs.getString("branch_id"))
                .salesOrderNo(readLongObject(rs, "sales_order_no"))
                .vehicleNumber(rs.getString("vehicle_no"))
                .farmLocation(rs.getString("farm_location"))
                .farmId(rs.getString("farm_id"))
                .farmName(rs.getString("farm_name"))
                .plannedQuantity(rs.getInt("planned_quantity"))
                .operationType(rs.getString("operation_type"))
                .scaleType(rs.getString("scale_type"))
                .status(status)
                .downloaded(downloaded)
                .standardBirdsPerCage(erpProperties.getStandardBirdsPerCage())
                .stableDurationMs(erpProperties.getStableDurationMs())
                .captureMode("AUTO")
                .completionPhotoRequired(true)
                .geofenceRadiusMeters(erpProperties.getDefaultGeofenceRadiusMeters());

        if (detail) {
            builder.farmLatitude(rs.getDouble("farm_latitude"))
                    .farmLongitude(rs.getDouble("farm_longitude"))
                    .farmerId(rs.getString("farmer_id"))
                    .farmerName(rs.getString("farmer_name"))
                    .farmerMobile(rs.getString("farmer_mobile"))
                    .traderId(rs.getString("trader_id"))
                    .traderName(rs.getString("trader_name"))
                    .traderMobile(null)
                    .productId(rs.getString("product_id"))
                    .productName(rs.getString("product_name"))
                    .toleranceMin(rs.getDouble("tolerance_min"))
                    .toleranceMax(rs.getDouble("tolerance_max"));
        } else {
            builder.farmLatitude(0)
                    .farmLongitude(0)
                    .toleranceMin(0)
                    .toleranceMax(0);
        }
        return builder.build();
    }

    private static Instant readInstant(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static Long readLongObject(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static ScheduleStatus mapStatus(String postedFlag, String npickStatus, String orderSource) {
        if ("Y".equalsIgnoreCase(postedFlag)) {
            return ScheduleStatus.COMPLETED;
        }
        if ("DOWNLOAD".equalsIgnoreCase(npickStatus) || "MOBILE_DOWNLOADED".equalsIgnoreCase(orderSource)) {
            return ScheduleStatus.DOWNLOADED; // ERP stores downloaded status as 'Y' in NPICK_STATUS. DOWNLOADED changed to Y
        }
        if (npickStatus != null && npickStatus.toUpperCase(Locale.ROOT).contains("PROGRESS")) {
            return ScheduleStatus.IN_PROGRESS;
        }
        return ScheduleStatus.ASSIGNED;
    }

}
