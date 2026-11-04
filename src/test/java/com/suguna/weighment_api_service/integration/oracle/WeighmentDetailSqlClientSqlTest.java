package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WeighmentDetailSqlClientSqlTest {

    @Test
    void insertSqlIncludesLiveReadingAvgWhenEnabled() {
        OracleErpProperties props = new OracleErpProperties();
        props.setWeighmentLiveReadingAvgEnabled(true);
        String sql = WeighmentDetailSqlClient.buildInsertDtlSql(props);
        assertTrue(sql.contains("LIVE_READING_AVG"));
        assertTrue(sql.matches("(?s).*avg_bird_wt,\\s+LIVE_READING_AVG,\\s+tolerance_status.*"));
    }

    @Test
    void insertSqlCommaBetweenAvgBirdWtAndToleranceWhenLiveAvgDisabled() {
        OracleErpProperties props = new OracleErpProperties();
        props.setWeighmentLiveReadingAvgEnabled(false);
        String sql = WeighmentDetailSqlClient.buildInsertDtlSql(props);
        assertTrue(sql.matches("(?s).*avg_bird_wt,\\s+tolerance_status.*"));
        assertTrue(!sql.contains("LIVE_READING_AVG"));
    }

    @Test
    void listSqlSelectsLiveReadingAvgWhenEnabled() {
        OracleErpProperties props = new OracleErpProperties();
        props.setWeighmentLiveReadingAvgEnabled(true);
        String sql = WeighmentDetailSqlClient.buildListDtlSql(props);
        assertTrue(sql.contains("live_reading_avg"));
        assertTrue(sql.contains("LIVE_READING_AVG"));
    }
}
