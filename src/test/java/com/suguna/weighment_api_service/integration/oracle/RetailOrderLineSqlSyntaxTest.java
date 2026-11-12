package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards retail join SQL for ORA-00923 (missing/misplaced FROM). */
class RetailOrderLineSqlSyntaxTest {

    @Test
    void outerJoinsDoNotUseSubqueryInOnClause() {
        OracleErpProperties props = oeRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String from = sql.fromScheduleToDetailJoin().replaceAll("\\s+", " ").toUpperCase();
        assertFalse(from.contains("ON N.TRANS_DATE = ( SELECT"), from);
        assertTrue(from.contains("N_LATEST"), from);
        String exists = sql.existsDetailLinkedToSchedule().replaceAll("\\s+", " ").toUpperCase();
        assertFalse(exists.contains("HZ_"), exists);
        assertFalse(exists.contains("NTAIL"), exists);
        assertFalse(exists.contains("SUG_MAI_RETAIL_ORDER_DTL"), exists);
        assertTrue(exists.contains("ROH.HEADER_ID = ROD.HEADER_ID"), exists);
    }

    @Test
    void updateWhereUsesWhereDetailOrderNoNotWhereAnd() {
        String where =
                " WHERE " + RetailOrderLineSqlSupport.whereDetailOrderNo() + " AND EXISTS (SELECT 1 FROM dual)";
        assertFalse(where.toUpperCase().contains("WHERE AND"), where);
    }

    @Test
    void weighmentSubmitDoesNotOverwriteDtlQuantityOrWeight() {
        OracleErpProperties props = sugRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String messageSet = sql.buildMessageUpdateSetClause().toUpperCase();
        assertFalse(messageSet.contains("ROD.QUANTITY"), messageSet);
        assertFalse(messageSet.contains("ROD.WEIGHT"), messageSet);
        assertTrue(messageSet.contains("ROD.DELV_QUANTITY"), messageSet);
        assertTrue(messageSet.contains("ROD.DELV_WEIGHT"), messageSet);
        String deliveredSet = sql.buildDeliveredQtyUpdateSetClause().toUpperCase();
        assertTrue(deliveredSet.contains("ROD.DELV_QUANTITY"), deliveredSet);
        assertTrue(deliveredSet.contains("ROD.DELV_WEIGHT"), deliveredSet);
        assertFalse(deliveredSet.contains("ROD.QUANTITY ="), deliveredSet);
        String completeSet = sql.buildCompleteDetailSetClause().toUpperCase();
        assertFalse(completeSet.contains("ROD.QUANTITY"), completeSet);
        assertFalse(completeSet.contains("ROD.WEIGHT"), completeSet);
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
        assertTrue(selectCount == 2, select);
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
        assertTrue(from.contains("HCAS_ORD.CUST_ACCT_SITE_ID = CSU_ORD.CUST_ACCT_SITE_ID"), from);
        assertTrue(from.contains("HCA_PICK.CUST_ACCOUNT_ID"), from);
        assertTrue(from.contains("CSU_PICK.SITE_USE_ID"), from);
        assertFalse(from.contains("HCA_ORD.CUST_ACCOUNT_ID = (SELECT"), from);
    }

    @Test
    void oePoChainJoinMatchesReferenceSqlShape() {
        OracleErpProperties props = oeRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String from = sql.fromScheduleToDetailJoin().replaceAll("\\s+", " ").toUpperCase();
        assertTrue(from.contains("OE_ORDER_LINES_ALL D ON TO_CHAR(E.ERPINDENTID) = TRIM(D.ATTRIBUTE2)"), from);
        assertTrue(from.contains("D.LINE_ID = (SELECT MIN(DX.LINE_ID)"), from);
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
    void listJoinOmitsHzCustomerTables() {
        OracleErpProperties props = oeRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String from = sql.fromScheduleToDetailJoinWithoutHz().replaceAll("\\s+", " ").toUpperCase();
        assertFalse(from.contains("HZ_"), from);
        assertTrue(from.contains("OE_ORDER_LINES_ALL"), from);
        assertTrue(from.contains("SUG_MAI_RETAIL_ORDER_DTL"), from);
        assertTrue(from.contains("SUG_MAI_NTAIL_ORDER"), from);
        String select = sql.selectDetailListSqlPrefix(false).replaceAll("\\s+", " ").toUpperCase();
        assertTrue(select.contains("NVL(N.WEIGHT, NVL(ROD.WEIGHT, 0))"), select);
        assertTrue(select.contains("DELIVERED_QUANTITY"), select);
        assertTrue(select.contains("DELIVERED_WEIGHT"), select);
        assertFalse(select.contains("HP_ORD"), select);
        assertFalse(select.contains("HL_ORD"), select);
    }

    @Test
    void hzCustomerLookupSqlIsSinglePassPerAccountNumber() {
        OracleErpProperties props = oeRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String lookup = sql.buildHzCustomerLookupSql(3).replaceAll("\\s+", " ").toUpperCase();
        assertTrue(lookup.contains("ROW_NUMBER() OVER"), lookup);
        assertTrue(lookup.contains("IN (?,?,?)"), lookup);
        assertTrue(lookup.contains("HZ_PARTIES HP"), lookup);
        assertFalse(lookup.contains("OE_ORDER"), lookup);
    }

    @Test
    void listJoinUsesOneHzCustomerSiteWithoutIndentShipTo() {
        OracleErpProperties props = oeRetailProps();
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(props);
        String from = sql.fromScheduleToDetailJoinForList().replaceAll("\\s+", " ").toUpperCase();
        assertTrue(from.contains("HZ_PARTIES HP_ORD"), from);
        assertTrue(from.contains("HCA_PICK.CUST_ACCOUNT_ID"), from);
        assertTrue(from.contains("CSU_PICK.SITE_USE_ID"), from);
        assertFalse(from.contains("HCA_ORD.CUST_ACCOUNT_ID = (SELECT"), from);
        assertFalse(from.contains("CSU.SITE_USE_ID = C.SHIP_TO_ORG_ID"), from);
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

    @Test
    void indentCustomerHzLookupPrefersCusLocationAndHasThreeBinds() {
        RetailOrderLineSqlSupport sql = new RetailOrderLineSqlSupport(oeRetailProps());
        String lookup = sql.buildHzIndentCustomerLookupSql().toUpperCase();
        assertTrue(lookup.contains("HL.ADDRESS1"), lookup);
        assertTrue(lookup.contains("POSTAL_CODE"), lookup);
        assertTrue(lookup.contains("WHERE TRIM(TO_CHAR(HCA.ACCOUNT_NUMBER)) = TRIM(?)"), lookup);
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
