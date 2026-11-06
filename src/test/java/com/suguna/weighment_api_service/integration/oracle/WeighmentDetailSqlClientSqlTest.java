package com.suguna.weighment_api_service.integration.oracle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WeighmentDetailSqlClientSqlTest {

    @Test
    void insertSqlIncludesLiveReadingAvg() {
        String sql = WeighmentDetailSqlClient.buildInsertDtlSql();
        assertTrue(sql.contains("LIVE_READING_AVG"));
        assertTrue(sql.matches("(?s).*avg_bird_wt,\\s+LIVE_READING_AVG,\\s+tolerance_status.*"));
    }

    @Test
    void listSqlSelectsLiveReadingAvg() {
        String sql = WeighmentDetailSqlClient.buildListDtlSql();
        assertTrue(sql.contains("live_reading_avg"));
        assertTrue(sql.contains("LIVE_READING_AVG"));
    }
}
