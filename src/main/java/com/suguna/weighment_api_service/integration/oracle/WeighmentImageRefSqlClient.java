package com.suguna.weighment_api_service.integration.oracle;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Persists {@link OracleErpColumns#WEIGHMENT_IMAGE_REF} rows after filesystem store. */
@Component
public class WeighmentImageRefSqlClient {

    private static final int MAX_FILE_KEY = 512;
    private static final int MAX_UPLOADED_BY = 64;
    private static final int MAX_APPLICATION = 32;
    private static final int ID_RETRY_ATTEMPTS = 5;

    /**
     * Identity {@code ID} on TESTPRE drifted behind {@code MAX(ID)} (ORA-00001 on
     * {@code PK_SUG_MAI_WEIGHMENT_IMAGE_REF}). Assign the next id explicitly.
     */
    static final String INSERT_SQL =
            """
            INSERT INTO """
                    + " "
                    + qualifiedTable()
                    + """
             (
                ID,
                APPLICATION,
                SCHEDULE_ID,
                ORDER_NO,
                IMAGE_DOMAIN,
                SALE_TYPE,
                IMAGE_CATEGORY,
                FILE_KEY,
                CAPTURED_AT,
                UPLOADED_BY
            )
            SELECT (SELECT NVL(MAX(id), 0) + 1 FROM """
                    + " "
                    + qualifiedTable()
                    + """
                   ),
                   ?, ?, ?, ?, ?, ?, ?, ?, ?
              FROM dual""";

    private final JdbcTemplate jdbcTemplate;

    public WeighmentImageRefSqlClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertImageRef(WeighmentImageRefInsert row) {
        DuplicateKeyException last = null;
        for (int attempt = 0; attempt < ID_RETRY_ATTEMPTS; attempt++) {
            try {
                jdbcTemplate.update(
                        INSERT_SQL,
                        truncate(row.application(), MAX_APPLICATION),
                        row.scheduleId(),
                        row.orderNo(),
                        row.imageDomain(),
                        row.saleType(),
                        row.imageCategory(),
                        truncate(row.fileKey(), MAX_FILE_KEY),
                        row.capturedAt() == null ? null : Timestamp.from(row.capturedAt()),
                        truncate(row.uploadedBy(), MAX_UPLOADED_BY));
                return;
            } catch (DuplicateKeyException ex) {
                last = ex;
            }
        }
        throw last;
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
