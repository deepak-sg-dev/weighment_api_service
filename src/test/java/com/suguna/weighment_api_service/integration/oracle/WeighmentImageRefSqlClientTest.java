package com.suguna.weighment_api_service.integration.oracle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WeighmentImageRefSqlClientTest {

    @Test
    void insertTargetsSugWeighmentImageRefTable() {
        assertTrue(WeighmentImageRefSqlClient.qualifiedTable().contains("SUG_MAI_WEIGHMENT_IMAGE_REF"));
    }
}
