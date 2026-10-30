package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class OracleLiftingScheduleRowMapper implements RowMapper<LiftingSchedule> {

    @Override
    public LiftingSchedule mapRow(ResultSet rs, int rowNum) throws SQLException {
        LiftingSchedule schedule = new LiftingSchedule();
        schedule.setScheduleId(requiredString(rs, "SCHEDULE_ID", "SCHEDULEID", "ORDER_ID"));
        schedule.setSupervisorId(string(rs, "SUPERVISOR_ID", "SUPERVISORID"));
        schedule.setFarmId(string(rs, "FARM_ID", "FARMID"));
        schedule.setFarmName(string(rs, "FARM_NAME", "FARMNAME"));
        schedule.setFarmLatitude(number(rs, "FARM_LATITUDE", "LATITUDE", "FARM_LAT"));
        schedule.setFarmLongitude(number(rs, "FARM_LONGITUDE", "LONGITUDE", "FARM_LONG"));
        schedule.setGeofenceRadiusMeters(intValue(rs, "GEOFENCE_RADIUS_METERS", "GEOFENCE_RADIUS", "GEOFENCE_RAD"));
        schedule.setFarmerId(string(rs, "FARMER_ID", "FARMERID"));
        schedule.setFarmerName(string(rs, "FARMER_NAME", "FARMERNAME"));
        schedule.setFarmerMobile(string(rs, "FARMER_MOBILE", "FARMER_PHONE", "FARMER_MOBILENO"));
        schedule.setTraderId(string(rs, "TRADER_ID", "TRADERID"));
        schedule.setTraderName(string(rs, "TRADER_NAME", "TRADERNAME"));
        schedule.setTraderMobile(string(rs, "TRADER_MOBILE", "TRADER_PHONE", "TRADER_MOBILENO"));
        schedule.setProductId(string(rs, "PRODUCT_ID", "PRODUCT_CODE", "PRODUCTID"));
        schedule.setProductName(string(rs, "PRODUCT_NAME", "PRODUCTNAME"));
        schedule.setPlannedQuantity(intValue(rs, "PLANNED_QUANTITY", "PLANNED_QTY", "SCHEDULED_QTY"));
        schedule.setOperationType(string(rs, "OPERATION_TYPE", "OPERATIONTYPE", "BIRD_TYPE"));
        schedule.setScaleType(string(rs, "SCALE_TYPE", "SCALETYPE", "WEIGH_SCALE_TYPE"));
        schedule.setStatus(parseStatus(string(rs, "STATUS", "SCHEDULE_STATUS")));
        schedule.setDownloaded(booleanValue(rs, "DOWNLOADED", "IS_DOWNLOADED", "DOWNLOAD_FLAG"));
        schedule.setStandardBirdsPerCage(intValue(rs, "STANDARD_BIRDS_PER_CAGE", "BIRDS_PER_CAGE"));
        schedule.setStableDurationMs(longValue(rs, "STABLE_DURATION_MS", "STABLE_DURATION"));
        schedule.setToleranceMin(number(rs, "TOLERANCE_MIN", "TOL_MIN"));
        schedule.setToleranceMax(number(rs, "TOLERANCE_MAX", "TOL_MAX"));
        schedule.setCaptureMode(string(rs, "CAPTURE_MODE", "CAPTUREMODE"));
        schedule.setCompletionPhotoRequired(booleanValue(rs, "COMPLETION_PHOTO_REQUIRED", "PHOTO_REQUIRED"));
        return schedule;
    }

    private static ScheduleStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return ScheduleStatus.ASSIGNED;
        }
        return ScheduleStatus.fromQueryValue(raw.replace(' ', '_'))
                .orElse(ScheduleStatus.ASSIGNED);
    }

    private static String requiredString(ResultSet rs, String... columns) throws SQLException {
        String value = string(rs, columns);
        if (value == null || value.isBlank()) {
            throw new SQLException("Missing schedule identifier column in Oracle result");
        }
        return value;
    }

    private static String string(ResultSet rs, String... columns) throws SQLException {
        for (String column : columns) {
            try {
                String value = rs.getString(column);
                if (value != null) {
                    return value;
                }
            } catch (SQLException ignored) {
                // try next alias
            }
        }
        return null;
    }

    private static int intValue(ResultSet rs, String... columns) throws SQLException {
        for (String column : columns) {
            try {
                int value = rs.getInt(column);
                if (!rs.wasNull()) {
                    return value;
                }
            } catch (SQLException ignored) {
                // try next alias
            }
        }
        return 0;
    }

    private static long longValue(ResultSet rs, String... columns) throws SQLException {
        for (String column : columns) {
            try {
                long value = rs.getLong(column);
                if (!rs.wasNull()) {
                    return value;
                }
            } catch (SQLException ignored) {
                // try next alias
            }
        }
        return 0L;
    }

    private static double number(ResultSet rs, String... columns) throws SQLException {
        for (String column : columns) {
            try {
                double value = rs.getDouble(column);
                if (!rs.wasNull()) {
                    return value;
                }
            } catch (SQLException ignored) {
                // try next alias
            }
        }
        return 0D;
    }

    private static boolean booleanValue(ResultSet rs, String... columns) throws SQLException {
        for (String column : columns) {
            try {
                String text = rs.getString(column);
                if (text != null) {
                    return "Y".equalsIgnoreCase(text)
                            || "1".equals(text)
                            || "TRUE".equalsIgnoreCase(text);
                }
                boolean bool = rs.getBoolean(column);
                if (!rs.wasNull()) {
                    return bool;
                }
            } catch (SQLException ignored) {
                // try next alias
            }
        }
        return false;
    }
}
