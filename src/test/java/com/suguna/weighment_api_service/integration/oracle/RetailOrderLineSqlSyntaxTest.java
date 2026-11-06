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
        OracleErpProperties props = sugRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String statement = sql.buildLoadOrderMessageStateSql().replaceAll("\\s+", " ").trim();
        assertTrue(statement.matches("(?is)SELECT .+ FROM .+"), statement);
        assertTrue(!statement.contains(", FROM"), statement);
    }

    @Test
    void batchListSelectHasSingleSelectKeyword() {
        OracleErpProperties props = sugRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String select = (sql.selectDetailListSqlPrefix()
                        + sql.selectDelvStatusColumn()
                        + sql.selectDelvStartedColumn()
                        + sql.selectRetailOtpColumns()
                        + sql.selectDetailGpsColumns()
                        + sql.fromScheduleToDetailJoin())
                .replaceAll("\\s+", " ")
                .trim();
        int selectCount = select.split("(?i)\\bSELECT\\b", -1).length - 1;
        assertTrue(selectCount == 1, select);
        assertTrue(select.startsWith("SELECT "), select);
        assertTrue(select.contains(" lifting_schedule_id,"), select);
        assertTrue(select.contains(" FROM sug_mai_birds_lifting_order "), select);
    }

    @Test
    void orderedCustomerHzJoinUsesSiteUsesViaAcctSitesNotCustAccountIdOnSiteUses() {
        OracleErpProperties props = oeRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String from = sql.fromScheduleToDetailJoin().replaceAll("\\s+", " ").toUpperCase();
        assertFalse(from.contains("CSU_ORD.CUST_ACCOUNT_ID"), from);
        assertTrue(from.contains("HCAS_ORD.CUST_ACCOUNT_ID = HCA_ORD.CUST_ACCOUNT_ID"), from);
        assertTrue(from.contains("CSU_ORD.CUST_ACCT_SITE_ID = HCAS_ORD.CUST_ACCT_SITE_ID"), from);
    }

    @Test
    void oePoChainJoinMatchesReferenceSqlShape() {
        OracleErpProperties props = oeRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String from = sql.fromScheduleToDetailJoin().replaceAll("\\s+", " ").toUpperCase();
        assertTrue(from.contains("OE_ORDER_LINES_ALL D ON TO_CHAR(E.ERPINDENTID) = TRIM(D.ATTRIBUTE2)"), from);
        assertTrue(from.contains("C.ORIG_SYS_DOCUMENT_REF = B.SEGMENT1"), from);
        assertTrue(from.contains("HZ_CUST_SITE_USES_ALL"), from);
        String whereRead = sql.whereLiftingScheduleForRetailRead().replaceAll("\\s+", " ").toUpperCase();
        assertTrue(whereRead.contains("NVL(E.SOURCE, 'X') = ?"), whereRead);
        assertTrue(whereRead.contains("NVL(E.APPLICATION, ?) = ?"), whereRead);
        assertFalse(whereRead.contains("MOBILEUSEREMPID"), whereRead);
        String whereSecure = sql.whereLiftingScheduleForRetailSecure().replaceAll("\\s+", " ").toUpperCase();
        assertTrue(whereSecure.contains("TRIM(E.MOBILEUSEREMPID) = TRIM(?)"), whereSecure);
    }

    @Test
    void sugRetailJoinDoesNotReferenceOeOrderTables() {
        OracleErpProperties props = sugRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String from = sql.fromScheduleToDetailJoin().replaceAll("\\s+", " ").toUpperCase();
        assertFalse(from.contains("OE_ORDER"), from);
        assertFalse(from.contains("PO_REQUISITION"), from);
        assertTrue(from.contains("SUG_MAI_RETAIL_ORDER_HDR"), from);
        assertTrue(from.contains("SUG_MAI_RETAIL_ORDER_DTL"), from);
    }

    @Test
    void addressSelectUsesAcctSiteLinesNotIndentLocationCode() {
        OracleErpProperties props = oeRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String select = sql.selectDetailCoreColumnList().replaceAll("\\s+", " ").toUpperCase();
        assertTrue(select.contains("HL_ORD.ADDRESS1"), select);
        assertTrue(select.contains("HL_ORD.CITY"), select);
        assertTrue(select.contains("CAST(HCAS_ORD.ADDRESS_TEXT AS VARCHAR2(4000))"), select);
        assertTrue(select.contains("CAST(HL_ORD.ADDRESS1 AS VARCHAR2(4000))"), select);
        assertFalse(select.contains("HCAS_ORD.CITY"), select);
        assertFalse(select.contains("CSU.LOCATION"), select);
        String from = sql.fromScheduleToDetailJoin().replaceAll("\\s+", " ").toUpperCase();
        assertTrue(from.contains("HZ_LOCATIONS HL_ORD"), from);
        assertTrue(from.contains("HZ_PARTY_SITES HPS_ORD"), from);
    }

    @Test
    void gpsSelectDoesNotNvlVarcharFlexAttributeWithNumericZero() {
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(oeRetailProps());
        String gps = sql.selectDetailGpsColumns().replaceAll("\\s+", " ").toUpperCase();
        assertTrue(!gps.contains("NVL(HCAS_ORD.ATTRIBUTE11, 0)"), gps);
        assertTrue(gps.contains("TO_NUMBER(TRIM(TO_CHAR(HCAS_ORD.ATTRIBUTE11)))"), gps);
    }

    @Test
    void listSelectFragmentsDoNotEndSelectWithTrailingCommaBeforeFrom() {
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(sugRetailProps());
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

    private static OracleErpProperties oeRetailProps() {
        OracleErpProperties props = new OracleErpProperties();
        props.setRetailOrderLinkMode("OE_PO_CHAIN");
        props.setRetailPoRequisitionJoinTrim(false);
        props.setRetailCustomerFromHzSiteEnabled(true);
        return props;
    }

    private static OracleErpProperties sugRetailProps() {
        OracleErpProperties props = new OracleErpProperties();
        props.setRetailOrderLinkMode("SUG_RETAIL_TABLES");
        props.setRetailCustomerFromHzSiteEnabled(false);
        return props;
    }
}
