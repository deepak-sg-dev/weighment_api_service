package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Assembled SQL for retail delivery start UPDATE (HZ mode). */
class DeliveryStartUpdateSqlTest {

    @Test
    void startDeliveryUpdateSqlIsWellFormed() {
        OracleErpProperties props = hzRetailProps();
        RetailOrderLineSqlSupport retailOrderSql = new RetailOrderLineSqlSupport(props);
        String sql = buildStartDeliveryUpdateSql(retailOrderSql);
        String normalized = sql.replaceAll("\\s+", " ").toUpperCase();
        assertFalse(normalized.contains("WHERE AND"), sql);
        assertFalse(normalized.contains("SYSDATEWHERE"), sql);
        assertTrue(normalized.contains("SYSDATE WHERE"), sql);
        assertTrue(normalized.contains("UPDATE SUG.SUG_MAI_RETAIL_ORDER_DTL ROD SET"), sql);
        assertTrue(normalized.contains("EXISTS ( SELECT 1 FROM"), sql);
        assertFalse(normalized.contains("SELECT 1 JOIN"), sql);
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

    private static OracleErpProperties hzRetailProps() {
        OracleErpProperties props = new OracleErpProperties();
        props.setOracleAppsSchema("APPS");
        props.setOracleSugSchema("SUG");
        props.setRetailCustomerFromHzSiteEnabled(true);
        props.setRetailOrderDeliveryStatusEnabled(true);
        props.setRetailOrderDeliveryStartEnabled(true);
        props.setRetailOrderHdrTable("SUG_MAI_RETAIL_ORDER_HDR");
        props.setRetailOrderDtlTable("SUG_MAI_RETAIL_ORDER_DTL");
        props.setApplicationCode("BRO-WEIGHMENT");
        props.setRetailLiftingSource("RETAIL");
        return props;
    }
}
