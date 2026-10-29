package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards retail join SQL for ORA-00923 (missing/misplaced FROM). */
class RetailOrderLineSqlSyntaxTest {

    @Test
    void updateWhereUsesWhereDetailOrderNoNotWhereAnd() {
        String where =
                " WHERE " + RetailOrderLineSqlSupport.whereDetailOrderNo() + " AND EXISTS (SELECT 1 FROM dual)";
        assertFalse(where.toUpperCase().contains("WHERE AND"), where);
    }

    @Test
    void loadOrderMessageStateSqlContainsFromAfterSelectList() {
        OracleErpProperties props = hzRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String statement = sql.buildLoadOrderMessageStateSql().replaceAll("\\s+", " ").trim();
        assertTrue(statement.matches("(?is)SELECT .+ FROM .+"), statement);
        assertTrue(!statement.contains(", FROM"), statement);
    }

    @Test
    void listSelectFragmentsDoNotEndSelectWithTrailingCommaBeforeFrom() {
        OracleErpProperties props = hzRetailProps();
        props.setRetailOrderOtpUseDtlColumn(true);
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String select = (sql.selectDetailCoreColumns()
                        + sql.selectDelvStatusColumn()
                        + sql.selectDelvStartedColumn()
                        + sql.selectRetailOtpColumns()
                        + sql.selectDetailGpsColumns()
                        + sql.fromScheduleToDetailJoin())
                .replaceAll("\\s+", " ")
                .trim();
        assertTrue(select.matches("(?is).+ retailer_longitude FROM .+"), select);
        assertTrue(!select.contains("longitude , FROM"), select);
    }

    private static OracleErpProperties hzRetailProps() {
        OracleErpProperties props = new OracleErpProperties();
        props.setOracleAppsSchema("APPS");
        props.setOracleSugSchema("SUG");
        props.setRetailCustomerFromHzSiteEnabled(true);
        props.setRetailCustomerGpsEnabled(true);
        props.setRetailOrderDeliveryStatusEnabled(true);
        props.setRetailOrderDeliveryStartEnabled(true);
        props.setRetailOrderHdrTable("SUG_MAI_RETAIL_ORDER_HDR");
        props.setRetailOrderDtlTable("SUG_MAI_RETAIL_ORDER_DTL");
        return props;
    }
}
