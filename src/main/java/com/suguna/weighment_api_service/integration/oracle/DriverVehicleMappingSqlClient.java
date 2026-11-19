package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.auth.DriverVehicleMappingRow;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Reads {@code SUG.SUG_MAI_LIFTING_VEHICLE_MST} for driver login. Plate match ignores spaces and
 * punctuation (same compact form as mobile {@code vehicleNo}).
 */
@Component
public class DriverVehicleMappingSqlClient {

    private static final String FIND_BY_VEHICLE_SQL =
            """
            SELECT TO_CHAR(v.trans_id) AS trans_id,
                   TRIM(v.vehicle_no) AS vehicle_no,
                   v.vehicle_type AS vehicle_type,
                   TO_CHAR(v.device_id) AS device_id,
                   CAST(NULL AS VARCHAR2(128)) AS androidid,
                   CAST(NULL AS VARCHAR2(128)) AS device_id_2,
                   v.user_type AS user_type,
                   TRIM(v.mobile_no) AS mobile_no,
                   v.from_date AS from_date,
                   v.to_date AS to_date
              FROM sug.sug_mai_lifting_vehicle_mst v
             WHERE REGEXP_REPLACE(UPPER(NVL(v.vehicle_no, '')), '[^A-Z0-9]', '') = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public DriverVehicleMappingSqlClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DriverVehicleMappingRow> findByCompactVehicleNo(String compactVehicleNo) {
        return jdbcTemplate.query(
                FIND_BY_VEHICLE_SQL,
                (rs, rowNum) -> new DriverVehicleMappingRow(
                        rs.getString("trans_id"),
                        rs.getString("vehicle_no"),
                        rs.getString("vehicle_type"),
                        rs.getString("device_id"),
                        rs.getString("androidid"),
                        rs.getString("device_id_2"),
                        rs.getString("user_type"),
                        rs.getString("mobile_no"),
                        toLocalDate(rs.getDate("from_date")),
                        toLocalDate(rs.getDate("to_date"))),
                compactVehicleNo);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
