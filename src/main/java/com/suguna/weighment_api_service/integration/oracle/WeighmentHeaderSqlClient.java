package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures {@code SUG_MAI_BIRDS_WEIGHMENT} exists for a mobile schedule.
 * Header rows are often created by ERP with a different {@code wm_supervisor_code} than
 * {@code sug_mai_birds_lifting_order.mobileuserempid}; submit matches by {@code schedule_no} only
 * after lifting-order ownership is validated in {@link com.suguna.weighment_api_service.service.TransactionService}.
 */
@Component
public class WeighmentHeaderSqlClient {

    private static final String ORDER_SUPERVISOR_MATCH =
            """
             (TRIM(o.mobileuserempid) = TRIM(?)
              OR LTRIM(TRIM(o.mobileuserempid), '0') = LTRIM(TRIM(?), '0'))
            """;

    private static final String COUNT_HEADER_BY_SCHEDULE_SQL =
            """
            SELECT COUNT(*)
              FROM sug_mai_birds_weighment w
             WHERE """
            + ScheduleIdSupport.WEIGHMENT_SCHEDULE_NO_MATCH;

    private static final String INSERT_HEADER_FROM_ORDER_SQL =
            """
            INSERT INTO sug_mai_birds_weighment (
                trans_id,
                schedule_no,
                wm_supervisor_code,
                farm_code,
                farm_name,
                branch_code,
                bird_type,
                ordered_qty,
                order_number,
                vehicle_no,
                dirver_name,
                status,
                npick_status,
                creation_date,
                source
            )
            SELECT sug_mai_birds_weighment_s.NEXTVAL,
                   o.erpindentid,
                   o.mobileuserempid,
                   o.farmercode,
                   NVL(f.farm_name_village, o.farmername),
                   o.branchcode,
                   NVL(NULLIF(TRIM(o.ordertype), ''), 'BROILER'),
                   NVL(o.no_of_birds, 0),
                   TO_CHAR(o.ordernumber),
                   o.vehicleno,
                   o.driver,
                   'Pending',
                   'Y',
                   SYSDATE,
                   NVL(o.source, 'MOBILE')
              FROM sug_mai_birds_lifting_order o
              LEFT JOIN sug_farm_mst_header f
                ON TRIM(f.farm_code) = TRIM(o.farmercode)
             WHERE """
            + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH
            + """
               AND """
            + ORDER_SUPERVISOR_MATCH;

    /**
     * Match weighment by indent {@code schedule_no} or sales {@code order_number} (same schedule id binds x4).
     */
    private static final String WEIGHMENT_INDENT_OR_ORDER_MATCH =
            """
             (
              TO_CHAR(w.schedule_no) = TRIM(?)
              OR TO_CHAR(w.schedule_no) = REGEXP_SUBSTR(TRIM(?), '^[^/]+')
              OR TRIM(w.order_number) = TRIM(?)
              OR TRIM(w.order_number) = REGEXP_SUBSTR(TRIM(?), '^[^/]+')
             )
            """;

    private static final String UPDATE_VEHICLE_SQL =
            """
            UPDATE sug_mai_birds_weighment w
               SET w.vehicle_no = NVL(?, w.vehicle_no),
                   w.dirver_name = NVL(?, w.dirver_name),
                   w.updated_date = SYSDATE
             WHERE """
                    + WEIGHMENT_INDENT_OR_ORDER_MATCH;

    private final JdbcTemplate jdbcTemplate;

    public WeighmentHeaderSqlClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void ensureHeaderForSchedule(String scheduleId, String supervisorCode) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        Integer count = jdbcTemplate.queryForObject(
                COUNT_HEADER_BY_SCHEDULE_SQL,
                Integer.class,
                normalizedScheduleId,
                normalizedScheduleId);
        if (count != null && count > 0) {
            return;
        }
        int inserted = jdbcTemplate.update(
                INSERT_HEADER_FROM_ORDER_SQL,
                normalizedScheduleId,
                normalizedScheduleId,
                supervisorCode,
                supervisorCode);
        if (inserted == 0) {
            throw new ResourceNotFoundException("Lifting order", scheduleId);
        }
    }

    /**
     * Updates {@code VEHICLE_NO} / {@code DIRVER_NAME} on existing weighment headers. No-op when no header yet
     * (start/PATCH can run before first weighment insert).
     */
    public int updateVehicle(String scheduleId, String vehicleNo, String driverName) {
        String normalized = ScheduleIdSupport.normalize(scheduleId);
        String plate = blankToNull(vehicleNo);
        String driver = blankToNull(driverName);
        if (plate == null && driver == null) {
            return 0;
        }
        return jdbcTemplate.update(
                UPDATE_VEHICLE_SQL,
                plate,
                driver,
                normalized,
                normalized,
                normalized,
                normalized);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
