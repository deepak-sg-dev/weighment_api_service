package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Assembled SQL for retail delivery start UPDATE (HZ mode). */
class DeliveryStartUpdateSqlTest {

    @Test
    void startDeliveryUpdateBindCountMatchesHzCustomerFilter() {
        OracleErpProperties props = sugRetailProps();
        props.setRetailCustomerFromHzSiteEnabled(true);
        RetailOrderLineSqlSupport retailOrderSql = new RetailOrderLineSqlSupport(props);
        String sql = buildStartDeliveryUpdateSql(retailOrderSql);
        int placeholders = countBindPlaceholders(sql);
        // startedAt + message + orderId + retailerId + 7 schedule EXISTS binds
        assertEquals(11, placeholders, sql);
    }

    @Test
    void startDeliveryUpdateSqlIsWellFormed() {
        OracleErpProperties props = sugRetailProps();
        RetailOrderLineSqlSupport retailOrderSql = new RetailOrderLineSqlSupport(props);
        String sql = buildStartDeliveryUpdateSql(retailOrderSql);
        String normalized = sql.replaceAll("\\s+", " ").toUpperCase();
        assertFalse(normalized.contains("WHERE AND"), sql);
        assertFalse(normalized.contains("SYSDATEWHERE"), sql);
        assertTrue(normalized.contains("SYSDATE WHERE"), sql);
        assertTrue(normalized.contains("UPDATE SUG.SUG_MAI_RETAIL_ORDER_DTL ROD SET"), sql);
        assertTrue(normalized.contains("EXISTS ( SELECT 1 FROM"), sql);
        assertFalse(normalized.contains("SELECT 1 JOIN"), sql);
        assertTrue(normalized.contains("TRIM(TO_CHAR(ROD.ORDER_NO)) = TRIM(?)"), sql);
        assertTrue(normalized.contains("ROH.HEADER_ID = ROD.HEADER_ID"), sql);
        int dtlCount = 0;
        int from = 0;
        while ((from = normalized.indexOf("SUG_MAI_RETAIL_ORDER_DTL", from)) >= 0) {
            dtlCount++;
            from += 1;
        }
        assertEquals(1, dtlCount, sql);
    }

    static String buildStartDeliveryUpdateSql(RetailOrderLineSqlSupport retailOrderSql) {
        String existsSchedule = retailOrderSql.existsDetailLinkedToSchedule();
        return retailOrderSql.buildDetailUpdatePrefix()
                + retailOrderSql.buildStartDeliverySetClause()
                + RetailOrderLineSqlSupport.updateWhereClause()
                + RetailOrderLineSqlSupport.whereDetailOrderNo()
                + retailOrderSql.andDetailCustomerCodeFilter().trim()
                + " AND "
                + existsSchedule;
    }

    private static int countBindPlaceholders(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                count++;
            }
        }
        return count;
    }

    private static OracleErpProperties sugRetailProps() {
        OracleErpProperties props = new OracleErpProperties();
        props.setRetailOrderLinkMode("SUG_RETAIL_TABLES");
        props.setRetailCustomerFromHzSiteEnabled(false);
        props.setApplicationCode("BRO-WEIGHMENT");
        props.setRetailLiftingSource("RETAIL");
        return props;
    }
}
