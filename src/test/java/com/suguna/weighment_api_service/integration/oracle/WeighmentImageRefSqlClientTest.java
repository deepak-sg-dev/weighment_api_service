package com.suguna.weighment_api_service.integration.oracle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WeighmentImageRefSqlClientTest {

    @Test
    void insertTargetsSugWeighmentImageRefTable() {
        assertTrue(WeighmentImageRefSqlClient.qualifiedTable().contains("SUG_MAI_WEIGHMENT_IMAGE_REF"));
    }

    @Test
    void insertAssignsNextIdFromMaxToAvoidIdentityPkCollision() {
        String sql = WeighmentImageRefSqlClient.INSERT_SQL.replaceAll("\\s+", " ").toUpperCase();
        assertTrue(sql.contains("NVL(MAX(ID), 0) + 1"));
        assertTrue(sql.contains("FROM DUAL"));
        assertTrue(sql.contains(" ID,"));
    }
}
