package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleLiftingProperties;
import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.MarkScheduleDownloadedRequest;
import com.suguna.weighment_api_service.dto.weighment.schedule.StartScheduleRequest;
import com.suguna.weighment_api_service.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calls {@code APPS.sug_mai_birds_lifting_pkg} procedures for lifting schedule data.
 * Procedure names and parameter names are configurable until ERP publishes final signatures.
 */
@Component
public class BirdsLiftingPackageClient {

    private static final Logger log = LoggerFactory.getLogger(BirdsLiftingPackageClient.class);

    private final JdbcTemplate jdbcTemplate;
    private final OracleLiftingProperties properties;
    private final OracleLiftingScheduleRowMapper rowMapper;

    public BirdsLiftingPackageClient(
            JdbcTemplate jdbcTemplate,
            OracleLiftingProperties properties,
            OracleLiftingScheduleRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
        this.rowMapper = rowMapper;
    }

    public List<LiftingSchedule> listSchedules(String supervisorId, Set<ScheduleStatus> statuses) {
        String procedure = properties.getProcedures().getListSchedules();
        requireConfigured(procedure, "app.oracle.lifting.procedures.list-schedules");

        Map<String, Object> in = baseSupervisorParams(supervisorId);
        in.put(properties.getParameters().getStatusList(), toStatusCsv(statuses));
        return executeCursorProcedure(procedure, in);
    }

    public LiftingSchedule getScheduleDetails(String supervisorId, String scheduleId) {
        OracleLiftingProperties.Procedures procedures = properties.getProcedures();
        String procedure = firstNonBlank(procedures.getGetSchedulePackage(), procedures.getGetScheduleDetails());
        requireConfigured(procedure, "app.oracle.lifting.procedures.get-schedule-details / get-schedule-package");

        Map<String, Object> in = baseSupervisorParams(supervisorId);
        in.put(properties.getParameters().getScheduleId(), scheduleId);

        List<LiftingSchedule> rows = executeCursorProcedure(procedure, in);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void markScheduleDownloaded(String supervisorId, String scheduleId, MarkScheduleDownloadedRequest request) {
        String procedure = properties.getProcedures().getMarkScheduleDownloaded();
        requireConfigured(procedure, "app.oracle.lifting.procedures.mark-schedule-downloaded");

        Map<String, Object> in = baseSupervisorParams(supervisorId);
        in.put(properties.getParameters().getScheduleId(), scheduleId);
        in.put(properties.getParameters().getDeviceId(), request.getDeviceId());
        in.put(properties.getParameters().getAppVersion(), request.getAppVersion());
        in.put(properties.getParameters().getDownloadedAt(), Timestamp.from(request.getDownloadedAt()));
        executeActionProcedure(procedure, in);
    }

    public void startSchedule(String supervisorId, String scheduleId, StartScheduleRequest request) {
        String procedure = properties.getProcedures().getStartSchedule();
        requireConfigured(procedure, "app.oracle.lifting.procedures.start-schedule");

        Map<String, Object> in = baseSupervisorParams(supervisorId);
        in.put(properties.getParameters().getScheduleId(), scheduleId);
        in.put(properties.getParameters().getDeviceId(), request.getDeviceId());
        in.put(properties.getParameters().getLocalTransactionId(), request.getLocalTransactionId());
        in.put(properties.getParameters().getStartedAt(), Timestamp.from(request.getStartedAt()));
        in.put(properties.getParameters().getVehicleRegistration(), request.getVehicleRegistration());
        in.put(properties.getParameters().getLatitude(), request.getLocation().getLatitude());
        in.put(properties.getParameters().getLongitude(), request.getLocation().getLongitude());
        executeActionProcedure(procedure, in);
    }

    public void logPackageProcedures() {
        String sql =
                """
                SELECT procedure_name
                  FROM all_procedures
                 WHERE owner = UPPER(?)
                   AND object_name = UPPER(?)
                   AND procedure_name IS NOT NULL
                 ORDER BY procedure_name
                """;
        List<String> names = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString(1),
                properties.getSchemaName(),
                properties.getPackageName());
        log.info(
                "Oracle package {}.{} exposes {} procedures: {}",
                properties.getSchemaName(),
                properties.getPackageName(),
                names.size(),
                names);
    }

    private List<LiftingSchedule> executeCursorProcedure(String procedureName, Map<String, Object> inParams) {
        try {
            String cursorParam = properties.getParameters().getResultCursor();
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                    .withSchemaName(properties.getSchemaName())
                    .withCatalogName(properties.getPackageName())
                    .withProcedureName(procedureName)
                    .returningResultSet(cursorParam, rowMapper);
            Map<String, Object> result = call.execute(inParams);
            @SuppressWarnings("unchecked")
            List<LiftingSchedule> rows = (List<LiftingSchedule>) result.get(cursorParam);
            return rows == null ? List.of() : rows;
        } catch (RuntimeException ex) {
            log.error("Oracle lifting package call failed: {}.{}", properties.getPackageName(), procedureName, ex);
            throw new ServiceUnavailableException("Unable to read lifting schedules from ERP");
        }
    }

    private void executeActionProcedure(String procedureName, Map<String, Object> inParams) {
        try {
            SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                    .withSchemaName(properties.getSchemaName())
                    .withCatalogName(properties.getPackageName())
                    .withProcedureName(procedureName);
            Map<String, Object> out = call.execute(inParams);
            assertSuccess(out);
        } catch (ServiceUnavailableException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Oracle lifting package action failed: {}.{}", properties.getPackageName(), procedureName, ex);
            throw new ServiceUnavailableException("Unable to update lifting schedule in ERP");
        }
    }

    private void assertSuccess(Map<String, Object> out) {
        Object statusValue = out.get(properties.getParameters().getReturnStatus());
        if (statusValue == null) {
            return;
        }
        String status = String.valueOf(statusValue);
        if ("S".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)) {
            return;
        }
        Object messageValue = out.get(properties.getParameters().getReturnMessage());
        String message = messageValue == null ? "ERP rejected schedule update" : String.valueOf(messageValue);
        throw new ServiceUnavailableException(message);
    }

    private Map<String, Object> baseSupervisorParams(String supervisorId) {
        Map<String, Object> params = new HashMap<>();
        params.put(properties.getParameters().getSupervisorId(), supervisorId);
        return params;
    }

    private static String toStatusCsv(Set<ScheduleStatus> statuses) {
        return statuses.stream().map(status -> status.name().toLowerCase(Locale.ROOT)).collect(Collectors.joining(","));
    }

    private static void requireConfigured(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configure Oracle procedure name: " + propertyName);
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
