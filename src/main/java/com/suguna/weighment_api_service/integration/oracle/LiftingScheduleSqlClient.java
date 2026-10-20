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

    private static final String MOBILE_STATUS_COLUMNS =
            """
                   NVL(o.download_flag, 'N') AS download_flag,
                   o.downloaded_date,
                   o.started_date,
                   o.lifting_status,
                   o.delivery_status
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
                   o.source AS order_source,
            """
            + MOBILE_STATUS_COLUMNS
            + """
              FROM sug_mai_birds_lifting_order o
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
                   o.source AS order_source,
            """
            + MOBILE_STATUS_COLUMNS
            + """
                   ,
                   NVL(v.farm_mst_latitude, 0) AS farm_latitude,
                   NVL(v.farm_mst_longitude, 0) AS farm_longitude,
                   NVL(o.tolerancepercent, 0) AS tolerance_percent,
                   NVL(o.avg_wt_low, 0) AS tolerance_min,
                   NVL(o.avg_wt_high, 0) AS tolerance_max,
                   o.farmercode AS farmer_id,
                   f.farmer_name AS farmer_name,
                   o.farmer_mobile_no AS farmer_mobile,
                   o.customercode AS trader_id,
                   o.cus_name AS trader_name,
                   NVL(o.breed, 'BROILER') AS product_id,
                   NVL(o.breed, 'Broiler') AS product_name
              FROM sug_mai_birds_lifting_order o
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

    /**
     * List API: only {@code sug_mai_birds_lifting_order} rows created for the mobile weighment app
     * ({@code source} MOBILE / MOBILE_DOWNLOADED) plus configured retail source (e.g. RETAIL).
     */
    private static final String LIST_ORDER_SOURCE_PREDICATE =
            """
             AND (UPPER(TRIM(NVL(o.source, 'X'))) LIKE 'MOBILE%'
                  OR UPPER(TRIM(NVL(o.source, 'X'))) = UPPER(?))
            """;

    private static final String MARK_DOWNLOADED_SQL =
            """
            UPDATE sug_mai_birds_lifting_order o
               SET o.download_flag = 'Y',
                   o.downloaded_date = ?,
                   o.lifting_status = 'DOWNLOADED',
                   o.updated_at = SYSDATE,
                   o.updated_by = ?
             WHERE """
            + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH
            + """
               AND """
            + ORDER_SUPERVISOR_MATCH;

    private static final String START_SCHEDULE_SQL =
            """
            UPDATE sug_mai_birds_lifting_order o
               SET o.vehicleno = NVL(?, o.vehicleno),
                   o.started_date = ?,
                   o.lifting_status = 'IN_PROGRESS',
                   o.updated_at = SYSDATE,
                   o.updated_by = ?
             WHERE """
            + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH
            + """
               AND """
            + ORDER_SUPERVISOR_MATCH;

    /**
     * After weighment submit: close lifting. Retail schedules get {@code delivery_status = PENDING}
     * when unset; wholesale leaves {@code delivery_status} unchanged.
     */
    private static final String COMPLETE_LIFTING_AFTER_WEIGHMENT_SQL =
            """
            UPDATE sug_mai_birds_lifting_order o
               SET o.lifting_status = 'COMPLETED',
                   o.delivery_status = CASE
                       WHEN UPPER(TRIM(?)) = 'RETAIL'
                            AND (o.delivery_status IS NULL OR TRIM(o.delivery_status) = '')
                       THEN 'PENDING'
                       ELSE o.delivery_status
                   END,
                   o.updated_at = SYSDATE,
                   o.updated_by = ?
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
                + LIST_ORDER_SOURCE_PREDICATE
                + lookbackPredicate()
                + buildStatusWhereClause(statuses);

        List<Object> args = new ArrayList<>();
        args.addAll(supervisorKeys);
        args.add(erpProperties.getApplicationCode());
        args.add(erpProperties.getApplicationCode());
        args.add(erpProperties.getRetailLiftingSource());
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
        int updated = jdbcTemplate.update(
                MARK_DOWNLOADED_SQL,
                Timestamp.from(request.getDownloadedAt()),
                trimUpdatedBy(supervisorCode),
                normalizedScheduleId,
                normalizedScheduleId,
                supervisorCode,
                supervisorCode);
        if (updated == 0) {
            throw new ResourceNotFoundException("Schedule", scheduleId);
        }
    }

    public void startSchedule(String supervisorCode, String scheduleId, StartScheduleRequest request) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        int updated = jdbcTemplate.update(
                START_SCHEDULE_SQL,
                request.getVehicleRegistration(),
                Timestamp.from(request.getStartedAt()),
                trimUpdatedBy(supervisorCode),
                normalizedScheduleId,
                normalizedScheduleId,
                supervisorCode,
                supervisorCode);
        if (updated == 0) {
            throw new ResourceNotFoundException("Schedule", scheduleId);
        }
        invokeOrderStatusValidation(scheduleId);
    }

    /**
     * @param saleTypeOrModule normalized sale type or business module (e.g. RETAIL, WHOLESALE)
     */
    public void markLiftingCompletedAfterWeighment(
            String supervisorCode, String scheduleId, String saleTypeOrModule) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        String saleToken = normalizeSaleTypeForDelivery(saleTypeOrModule);
        jdbcTemplate.update(
                COMPLETE_LIFTING_AFTER_WEIGHMENT_SQL,
                saleToken,
                trimUpdatedBy(supervisorCode),
                normalizedScheduleId,
                normalizedScheduleId,
                supervisorCode,
                supervisorCode);
    }

    private String normalizeSaleTypeForDelivery(String saleTypeOrModule) {
        if (saleTypeOrModule == null || saleTypeOrModule.isBlank()) {
            return "WHOLESALE";
        }
        String normalized = saleTypeOrModule.trim().toUpperCase(Locale.ROOT);
        if ("RETAIL".equals(normalized)) {
            return "RETAIL";
        }
        if (erpProperties.getRetailLiftingSource().equalsIgnoreCase(normalized)) {
            return "RETAIL";
        }
        return normalized;
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

    private static String trimUpdatedBy(String supervisorCode) {
        if (supervisorCode == null) {
            return null;
        }
        String trimmed = supervisorCode.trim();
        return trimmed.length() > 64 ? trimmed.substring(0, 64) : trimmed;
    }

    private String lookbackPredicate() {
        if (erpProperties.getScheduleListLookbackDays() <= 0) {
            return "";
        }
        return """
             AND (NVL(o.lifting_status, 'PENDING') <> 'COMPLETED'
                  OR o.order_request_date IS NULL
                  OR o.order_request_date >= TRUNC(SYSDATE) - ?)
            """;
    }

    private static String buildStatusWhereClause(Set<ScheduleStatus> statuses) {
        if (statuses.isEmpty()) {
            return " AND NVL(o.lifting_status, 'PENDING') <> 'COMPLETED'";
        }
        if (statuses.size() == 1 && statuses.contains(ScheduleStatus.COMPLETED)) {
            return " AND NVL(o.lifting_status, 'PENDING') = 'COMPLETED'";
        }

        List<String> parts = new ArrayList<>();
        for (ScheduleStatus status : statuses) {
            parts.add(statusPredicate(status));
        }

        StringBuilder clause = new StringBuilder(" AND (");
        if (!statuses.contains(ScheduleStatus.COMPLETED)) {
            clause.append("NVL(o.lifting_status, 'PENDING') <> 'COMPLETED' AND (");
        }
        clause.append(String.join(" OR ", parts));
        clause.append(statuses.contains(ScheduleStatus.COMPLETED) ? ")" : "))");
        return clause.toString();
    }

    private static String statusPredicate(ScheduleStatus status) {
        return switch (status) {
            case ASSIGNED ->
                    """
                    (NVL(o.download_flag, 'N') <> 'Y'
                     AND NVL(o.lifting_status, 'PENDING') = 'PENDING')
                    """;
            case DOWNLOADED ->
                    """
                    (NVL(o.download_flag, 'N') = 'Y'
                     AND NVL(o.lifting_status, 'PENDING') = 'DOWNLOADED')
                    """;
            case IN_PROGRESS -> "NVL(o.lifting_status, 'PENDING') = 'IN_PROGRESS'";
            case COMPLETED -> "NVL(o.lifting_status, 'PENDING') = 'COMPLETED'";
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
        String downloadFlag = rs.getString("download_flag");
        String liftingStatusRaw = rs.getString("lifting_status");
        String deliveryStatusRaw = rs.getString("delivery_status");
        ScheduleStatus status = mapScheduleStatus(liftingStatusRaw, downloadFlag);
        boolean downloaded = "Y".equalsIgnoreCase(downloadFlag);

        LiftingSchedule schedule = new LiftingSchedule();
        schedule.setScheduleId(rs.getString("schedule_id"));
        schedule.setSupervisorId(rs.getString("supervisor_code"));
        schedule.setOrderSource(rs.getString("order_source"));
        schedule.setScheduledAt(readInstant(rs, "order_request_date"));
        schedule.setStartedAt(readInstant(rs, "started_date"));
        schedule.setPlannedQuantityKg(rs.getDouble("planned_quantity_kg"));
        schedule.setDeclaredBodyWeightKg(rs.getDouble("body_wt"));
        schedule.setBranchCode(rs.getString("branch_code"));
        schedule.setBranchId(rs.getString("branch_id"));
        schedule.setSalesOrderNo(readLongObject(rs, "sales_order_no"));
        schedule.setVehicleNumber(rs.getString("vehicle_no"));
        schedule.setFarmLocation(rs.getString("farm_location"));
        schedule.setFarmId(rs.getString("farm_id"));
        schedule.setFarmName(rs.getString("farm_name"));
        schedule.setPlannedQuantity(rs.getInt("planned_quantity"));
        schedule.setOperationType(rs.getString("operation_type"));
        schedule.setScaleType(rs.getString("scale_type"));
        schedule.setStatus(status);
        schedule.setDownloaded(downloaded);
        schedule.setLiftingStatus(normalizeStatusToken(liftingStatusRaw));
        schedule.setDeliveryStatus(normalizeStatusToken(deliveryStatusRaw));
        schedule.setStandardBirdsPerCage(erpProperties.getStandardBirdsPerCage());
        schedule.setStableDurationMs(erpProperties.getStableDurationMs());
        schedule.setCaptureMode("AUTO");
        schedule.setCompletionPhotoRequired(true);
        schedule.setGeofenceRadiusMeters(erpProperties.getDefaultGeofenceRadiusMeters());

        if (detail) {
            schedule.setFarmLatitude(rs.getDouble("farm_latitude"));
            schedule.setFarmLongitude(rs.getDouble("farm_longitude"));
            schedule.setFarmerId(rs.getString("farmer_id"));
            schedule.setFarmerName(rs.getString("farmer_name"));
            schedule.setFarmerMobile(rs.getString("farmer_mobile"));
            schedule.setTraderId(rs.getString("trader_id"));
            schedule.setTraderName(rs.getString("trader_name"));
            schedule.setTraderMobile(null);
            schedule.setProductId(rs.getString("product_id"));
            schedule.setProductName(rs.getString("product_name"));
            schedule.setToleranceMin(rs.getDouble("tolerance_min"));
            schedule.setToleranceMax(rs.getDouble("tolerance_max"));
        } else {
            schedule.setFarmLatitude(0);
            schedule.setFarmLongitude(0);
            schedule.setToleranceMin(0);
            schedule.setToleranceMax(0);
        }
        return schedule;
    }

    private static Instant readInstant(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static Long readLongObject(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    static ScheduleStatus mapScheduleStatus(String liftingStatus, String downloadFlag) {
        String normalized = normalizeStatusToken(liftingStatus);
        if (normalized != null) {
            return switch (normalized) {
                case "COMPLETED", "CANCELLED" -> ScheduleStatus.COMPLETED;
                case "IN_PROGRESS" -> ScheduleStatus.IN_PROGRESS;
                case "DOWNLOADED" -> ScheduleStatus.DOWNLOADED;
                case "PENDING" -> ScheduleStatus.ASSIGNED;
                default -> ScheduleStatus.ASSIGNED;
            };
        }
        if ("Y".equalsIgnoreCase(downloadFlag)) {
            return ScheduleStatus.DOWNLOADED;
        }
        return ScheduleStatus.ASSIGNED;
    }

    private static String normalizeStatusToken(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

}
