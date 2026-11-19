package com.suguna.weighment_api_service.integration.oracle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DeliveryWeighmentHeaderSqlTest {

    @Test
    void completedDeliveryInsertUsesShopCustomerAndDoesNotTouchFarmStatusPending() {
        String sql = WeighmentHeaderSqlClient.INSERT_COMPLETED_DELIVERY_HEADER_SQL.replaceAll("\\s+", " ").toUpperCase();
        assertTrue(sql.contains("CUSTOMER_CODE"));
        assertTrue(sql.contains("CUSTOMER_NAME"));
        assertTrue(sql.contains("CUSTOMER_LOCATION"));
        assertTrue(sql.contains("HZ_CUST_ACCOUNTS"));
        assertTrue(sql.contains("HZ_PARTIES"));
        assertTrue(sql.contains("HZ_LOCATIONS"));
        assertFalse(sql.contains("AR_CUSTOMERS"));
        assertFalse(sql.contains("SUG_MAI_NTAIL_CUSTOMER_MST"));
        assertTrue(sql.contains("ORDER_NUMBER"));
        assertTrue(sql.contains("'DELIVERY'"));
        assertTrue(sql.contains("'COMPLETED'"));
        assertTrue(sql.contains("SUG_MAI_BIRDS_LIFTING_ORDER"));
        assertTrue(sql.contains("DC_DATE"));
        assertTrue(sql.contains("O.ORDER_REQUEST_DATE"));
        assertFalse(sql.contains("'PENDING'"));
        assertTrue(sql.contains("UPPER(TRIM(W.SOURCE)) = 'DELIVERY'"));
    }
}
