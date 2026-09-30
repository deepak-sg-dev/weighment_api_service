package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.domain.DeviceRegistration;
import com.suguna.weighment_api_service.domain.SupervisorProfile;
import com.suguna.weighment_api_service.exception.ApiException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Device-to-supervisor resolution using ERP tables referenced by {@code sug_mai_birds_lifting_pkg}
 * (e.g. {@code sug_mai_devicedata}, {@code sug_hr_emp_mst_mv}, {@code sug_organization_mv}).
 */
@Component
public class DeviceLoginSqlClient {

    private static final String DEVICE_LOOKUP_SQL =
            """
            SELECT d.androidid AS device_id,
                   d.status AS device_status,
                   b.emp_no AS supervisor_code,
                   NVL(TO_CHAR(d.emp_id), b.emp_no) AS supervisor_id,
                   b.name AS supervisor_name,
                   NVL(b.mobile_no, NVL(d.contact_number, '0000000000')) AS supervisor_mobile,
                   c.branch_id AS branch_id,
                   c.branch_code AS branch_code,
                   NVL(c.branch_name, c.branch_code) AS branch_name
              FROM sug_mai_devicedata d
              JOIN sug_hr_emp_mst_mv b
                ON d.emp_code = b.emp_no
               AND d.ledger_id = b.ledger_id
              JOIN sug_organization_mv c
                ON c.branch_id = d.organization_id
             WHERE d.application = ?
               AND (
                    d.androidid = ?
                 OR TO_CHAR(d.device_id) = ?
                 OR TO_CHAR(d.device_id_2) = ?
                 OR EXISTS (
                      SELECT 1
                        FROM sug_mai_lifting_reg_user r
                       WHERE r.device_id = ?
                         AND r.user_name = b.emp_no
                    )
               )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final OracleErpProperties erpProperties;

    public DeviceLoginSqlClient(JdbcTemplate jdbcTemplate, OracleErpProperties erpProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.erpProperties = erpProperties;
    }

    public Optional<DeviceRegistration> findDeviceRegistration(String deviceId) {
        List<DeviceRegistration> rows = jdbcTemplate.query(
                DEVICE_LOOKUP_SQL,
                (rs, rowNum) -> {
                    String status = rs.getString("device_status");
                    boolean enabled = status == null || !"I".equalsIgnoreCase(status.trim());
                    SupervisorProfile supervisor = SupervisorProfile.builder()
                            .id(rs.getString("supervisor_id"))
                            .code(rs.getString("supervisor_code"))
                            .name(rs.getString("supervisor_name"))
                            .mobile(rs.getString("supervisor_mobile"))
                            .branchId(rs.getString("branch_code"))
                            .branchName(rs.getString("branch_name"))
                            .active(enabled)
                            .build();
                    return DeviceRegistration.builder()
                            .deviceId(rs.getString("device_id"))
                            .enabled(enabled)
                            .authorized(true)
                            .supervisor(supervisor)
                            .build();
                },
                erpProperties.getApplicationCode(),
                deviceId,
                deviceId,
                deviceId,
                deviceId);
        return rows.stream().findFirst();
    }

    public DeviceRegistration requireDeviceRegistration(String deviceId) {
        return findDeviceRegistration(deviceId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.DEVICE_NOT_CONFIGURED,
                        "Device is not mapped to a supervisor in ERP"));
    }
}
