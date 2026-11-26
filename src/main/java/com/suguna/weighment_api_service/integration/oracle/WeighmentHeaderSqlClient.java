package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.domain.DeliveryCompletedWeighmentHeader;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
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
            + ScheduleIdSupport.WEIGHMENT_SCHEDULE_NO_MATCH
            + ScheduleIdSupport.WEIGHMENT_EXCLUDE_DELIVERY_SOURCE;

    static final String FIND_DELIVERY_HEADER_TRANS_ID_SQL =
            """
            SELECT w.trans_id
              FROM sug_mai_birds_weighment w
             WHERE """
            + ScheduleIdSupport.WEIGHMENT_SCHEDULE_NO_MATCH
            + """
               AND TRIM(w.order_number) = TRIM(?)
               AND UPPER(TRIM(w.source)) = 'DELIVERY'
             ORDER BY w.trans_id DESC
             FETCH FIRST 1 ROW ONLY
            """;

    /**
     * Same HZ shop profile as schedule list: {@code hz_cust_accounts.account_number} → {@code hz_parties.party_name}
     * + SHIP_TO site (address / GPS). Bind: customer code.
     */
    static final String HZ_SHOP_CUSTOMER_SQL =
            """
            SELECT customer_name, address, retailer_latitude, retailer_longitude
              FROM (
                SELECT hp.party_name AS customer_name,
                       NVL(
                           NULLIF(TRIM(
                               NVL(TRIM(CAST(hl.address1 AS VARCHAR2(4000))), '')
                               || NVL2(TRIM(CAST(hl.address2 AS VARCHAR2(4000))),
                                       ', ' || TRIM(CAST(hl.address2 AS VARCHAR2(4000))), '')
                               || NVL2(TRIM(CAST(hl.city AS VARCHAR2(4000))),
                                       ', ' || TRIM(CAST(hl.city AS VARCHAR2(4000))), '')
                           ), ''),
                           NULLIF(TRIM(CAST(hcas.address_text AS VARCHAR2(4000))), '')
                       ) AS address,
                       CASE WHEN REGEXP_LIKE(TRIM(TO_CHAR(hcas.attribute11)), '^-?\\d+(\\.\\d+)?$')
                            THEN TO_NUMBER(TRIM(TO_CHAR(hcas.attribute11))) END AS retailer_latitude,
                       CASE WHEN REGEXP_LIKE(TRIM(TO_CHAR(hcas.attribute10)), '^-?\\d+(\\.\\d+)?$')
                            THEN TO_NUMBER(TRIM(TO_CHAR(hcas.attribute10))) END AS retailer_longitude,
                       ROW_NUMBER() OVER (
                           PARTITION BY TRIM(TO_CHAR(hca.account_number))
                           ORDER BY hca.cust_account_id, csu.site_use_id
                       ) AS rn
                  FROM apps.hz_cust_accounts hca
                  JOIN apps.hz_parties hp
                    ON hp.party_id = hca.party_id
                  LEFT JOIN apps.hz_cust_acct_sites_all hcas
                    ON hcas.cust_account_id = hca.cust_account_id
                  LEFT JOIN apps.hz_cust_site_uses_all csu
                    ON csu.cust_acct_site_id = hcas.cust_acct_site_id
                   AND csu.site_use_code = 'SHIP_TO'
                   AND csu.status = 'A'
                  LEFT JOIN apps.hz_party_sites hps
                    ON hps.party_site_id = hcas.party_site_id
                  LEFT JOIN apps.hz_locations hl
                    ON hl.location_id = hps.location_id
                 WHERE TRIM(TO_CHAR(hca.account_number)) = TRIM(?)
              )
             WHERE rn = 1
            """;

    static final String INSERT_COMPLETED_DELIVERY_HEADER_SQL =
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
                customer_code,
                customer_name,
                customer_location,
                vehicle_no,
                dirver_name,
                no_of_birds,
                no_of_cages,
                empty_wt,
                gross_wt,
                net_wt,
                avg_wt,
                latitude,
                longitude,
                wm_start_time,
                wm_end_time,
                manual_dc_no,
                status,
                source,
                dc_date,
                creation_date,
                updated_date
            )
            SELECT sug_mai_birds_weighment_s.NEXTVAL,
                   o.erpindentid,
                   o.mobileuserempid,
                   o.farmercode,
                   o.farmername,
                   o.branchcode,
                   NVL(NULLIF(TRIM(o.ordertype), ''), 'BROILER'),
                   ?,
                   ?,
                   ?,
                   NVL(?, hz.customer_name),
                   hz.address,
                   o.vehicleno,
                   o.driver,
                   ?,
                   ?,
                   ?,
                   ?,
                   ?,
                   ?,
                   NVL(?, NULLIF(hz.retailer_latitude, 0)),
                   NVL(?, NULLIF(hz.retailer_longitude, 0)),
                   ?,
                   ?,
                   ?,
                   'Completed',
                   'DELIVERY',
                   o.order_request_date,
                   SYSDATE,
                   SYSDATE
              FROM sug_mai_birds_lifting_order o
              LEFT JOIN (
            """
            + HZ_SHOP_CUSTOMER_SQL
            + """
              ) hz ON 1 = 1
             WHERE """
            + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH
            + """
               AND """
            + ORDER_SUPERVISOR_MATCH
            + """
               AND NOT EXISTS (
                    SELECT 1
                      FROM sug_mai_birds_weighment w
                     WHERE """
            + ScheduleIdSupport.WEIGHMENT_SCHEDULE_NO_MATCH
            + """
                       AND TRIM(w.order_number) = TRIM(?)
                       AND UPPER(TRIM(w.source)) = 'DELIVERY'
               )
            """;

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
                dc_date,
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
                   o.order_request_date,
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

    private static final String FILL_DELIVERY_CUSTOMER_SQL =
            """
            MERGE INTO sug_mai_birds_weighment w
            USING (
            """
            + HZ_SHOP_CUSTOMER_SQL
            + """
            ) hz
            ON (w.trans_id = ?)
            WHEN MATCHED THEN UPDATE SET
                   w.customer_code = NVL(NULLIF(TRIM(w.customer_code), ''), ?),
                   w.customer_name = NVL(NULLIF(TRIM(w.customer_name), ''), NVL(?, hz.customer_name)),
                   w.customer_location = NVL(NULLIF(TRIM(w.customer_location), ''), hz.address),
                   w.latitude = NVL(w.latitude, NULLIF(hz.retailer_latitude, 0)),
                   w.longitude = NVL(w.longitude, NULLIF(hz.retailer_longitude, 0)),
                   w.updated_date = SYSDATE
            """;

    private static final String UPDATE_VEHICLE_SQL =
            """
            UPDATE sug_mai_birds_weighment w
               SET w.vehicle_no = NVL(?, w.vehicle_no),
                   w.dirver_name = NVL(?, w.dirver_name),
                   w.vehicle_type = NVL(?, w.vehicle_type),
                   w.updated_date = SYSDATE
             WHERE """
                    + WEIGHMENT_INDENT_OR_ORDER_MATCH
                    + ScheduleIdSupport.WEIGHMENT_EXCLUDE_DELIVERY_SOURCE;

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
     * One header per completed shop. Copies farm/vehicle from the lifting indent; does not update the farm row.
     *
     * @return {@code trans_id} of the delivery header
     */
    public long ensureCompletedDeliveryHeader(DeliveryCompletedWeighmentHeader header) {
        String scheduleId = ScheduleIdSupport.normalize(header.getScheduleId());
        String orderNo = header.getRetailOrderNo() == null ? "" : header.getRetailOrderNo().trim();
        String supervisorCode = header.getSupervisorCode();
        String customerCode = blankToNull(header.getCustomerCode());
        String customerName = blankToNull(header.getCustomerName());
        int inserted = jdbcTemplate.update(
                INSERT_COMPLETED_DELIVERY_HEADER_SQL,
                header.getOrderedQty(),
                orderNo,
                customerCode,
                customerName,
                header.getNoOfBirds(),
                header.getNoOfCages(),
                header.getEmptyWt(),
                header.getGrossWt(),
                header.getNetWt(),
                header.getAvgWt(),
                header.getLatitude(),
                header.getLongitude(),
                timestamp(header.getWmStartTime()),
                timestamp(header.getWmEndTime()),
                blankToNull(header.getManualDcNo()),
                customerCode,
                scheduleId,
                scheduleId,
                supervisorCode,
                supervisorCode,
                scheduleId,
                scheduleId,
                orderNo);
        Long transId = findDeliveryHeaderTransId(scheduleId, orderNo);
        if (transId != null) {
            if (inserted == 0) {
                fillDeliveryCustomer(transId, customerCode, customerName);
            }
            return transId;
        }
        if (inserted == 0) {
            throw new ResourceNotFoundException(
                    "Lifting order for delivery weighment header, schedule " + scheduleId);
        }
        throw new ResourceNotFoundException(
                "Delivery weighment header for order " + orderNo + " schedule " + scheduleId);
    }

    private void fillDeliveryCustomer(long transId, String customerCode, String customerName) {
        jdbcTemplate.update(
                FILL_DELIVERY_CUSTOMER_SQL,
                customerCode,
                transId,
                customerCode,
                customerName);
    }

    public Long findDeliveryHeaderTransId(String scheduleId, String retailOrderNo) {
        String normalized = ScheduleIdSupport.normalize(scheduleId);
        String orderNo = retailOrderNo == null ? "" : retailOrderNo.trim();
        List<Long> ids = jdbcTemplate.query(
                FIND_DELIVERY_HEADER_TRANS_ID_SQL,
                (rs, rowNum) -> rs.getLong("trans_id"),
                normalized,
                normalized,
                orderNo);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private static Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    /**
     * Updates {@code VEHICLE_NO} / {@code DIRVER_NAME} on existing weighment headers. No-op when no header yet
     * (start/PATCH can run before first weighment insert).
     */
    public int updateVehicle(String scheduleId, String vehicleNo, String driverName) {
        return updateVehicle(scheduleId, vehicleNo, driverName, null);
    }

    public int updateVehicle(String scheduleId, String vehicleNo, String driverName, String vehicleType) {
        String normalized = ScheduleIdSupport.normalize(scheduleId);
        String plate = blankToNull(vehicleNo);
        String driver = blankToNull(driverName);
        String type = blankToNull(vehicleType);
        if (plate == null && driver == null && type == null) {
            return 0;
        }
        return jdbcTemplate.update(
                UPDATE_VEHICLE_SQL,
                plate,
                driver,
                type,
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
