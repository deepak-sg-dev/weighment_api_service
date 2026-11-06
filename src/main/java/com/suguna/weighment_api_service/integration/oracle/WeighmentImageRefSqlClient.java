package com.suguna.weighment_api_service.integration.oracle;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Persists {@link OracleErpColumns#WEIGHMENT_IMAGE_REF} rows after filesystem store. */
@Component
public class WeighmentImageRefSqlClient {

    private static final int MAX_FILE_KEY = 512;
    private static final int MAX_UPLOADED_BY = 64;
    private static final int MAX_APPLICATION = 32;

    private final String insertSql;

    private final JdbcTemplate jdbcTemplate;

    public WeighmentImageRefSqlClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertSql =
                """
                INSERT INTO """
                        + " "
                        + qualifiedTable()
                        + """
                 (
                    APPLICATION,
                    SCHEDULE_ID,
                    ORDER_NO,
                    IMAGE_DOMAIN,
                    SALE_TYPE,
                    IMAGE_CATEGORY,
                    FILE_KEY,
                    CAPTURED_AT,
                    UPLOADED_BY
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    }

    public void insertImageRef(WeighmentImageRefInsert row) {
        jdbcTemplate.update(
                insertSql,
                truncate(row.application(), MAX_APPLICATION),
                row.scheduleId(),
                row.orderNo(),
                row.imageDomain(),
                row.saleType(),
                row.imageCategory(),
                truncate(row.fileKey(), MAX_FILE_KEY),
                row.capturedAt() == null ? null : Timestamp.from(row.capturedAt()),
                truncate(row.uploadedBy(), MAX_UPLOADED_BY));
    }

    static String qualifiedTable() {
        return OracleErpColumns.SCHEMA_SUG + "." + OracleErpColumns.WEIGHMENT_IMAGE_REF;
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLen ? trimmed : trimmed.substring(0, maxLen);
    }

    public record WeighmentImageRefInsert(
            String application,
            String scheduleId,
            String orderNo,
            String imageDomain,
            String saleType,
            String imageCategory,
            String fileKey,
            Instant capturedAt,
            String uploadedBy) {}
}
