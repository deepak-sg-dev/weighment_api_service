package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import org.springframework.stereotype.Component;

/**
 * Links a retail lifting schedule ({@code sug_mai_birds_lifting_order.erpindentid}) to shop lines on
 * {@code sug_mai_retail_order_hdr} / {@code sug_mai_retail_order_dtl}.
 *
 * <p>Default ({@code SUG_RETAIL_TABLES}): no {@code oe_order_*}, PO, or HZ — HDR join only, DTL for line data.
 * Legacy {@code OE_PO_CHAIN} keeps the ERP OE line ({@code attribute2}) + PO + optional HZ customer path.
 */
@Component
public class RetailOrderLineSqlSupport {

    /** Two binds: same supervisor code (TRIM + zero-stripped match), same as reference retail list SQL. */
    private static final String LIFTING_SUPERVISOR_MATCH =
            """
             (TRIM(e.mobileuserempid) = TRIM(?)
              OR LTRIM(TRIM(e.mobileuserempid), '0') = LTRIM(TRIM(?), '0'))
            """;

    private final OracleErpProperties erpProperties;

    public RetailOrderLineSqlSupport(OracleErpProperties erpProperties) {
        this.erpProperties = erpProperties;
    }

    /** {@code rod} = detail line, {@code roh} = header, {@code e} = lifting schedule row. */
    public String fromScheduleToDetailJoin() {
        return fromScheduleToDetailJoin("rod");
    }

    /** Same join chain with a custom detail-line alias (for correlated {@code EXISTS} on UPDATE). */
    public String fromScheduleToDetailJoin(String detailAlias) {
        String rod = detailAlias == null || detailAlias.isBlank() ? "rod" : detailAlias.trim();
        String hdr = OracleErpColumns.SCHEMA_SUG + "." + OracleErpColumns.RETAIL_ORDER_HDR;
        String dtl = OracleErpColumns.SCHEMA_SUG + "." + OracleErpColumns.RETAIL_ORDER_DTL;
        String apps = OracleErpColumns.SCHEMA_APPS;
        if (erpProperties.isRetailOrderSugTablesLink()) {
            return "\n              FROM sug_mai_birds_lifting_order e\n"
                    + joinQualifiedTable(hdr, "roh ON " + hdrToLiftingMatchPredicate("roh"))
                    + joinQualifiedTable(dtl, rod + " ON roh.header_id = " + rod + ".header_id\n")
                    + joinOrderedCustomerHzSite(apps, rod);
        }
        return "\n              FROM sug_mai_birds_lifting_order e\n"
                + joinAppsTable(
                        apps, "oe_order_lines_all d ON TO_CHAR(e.erpindentid) = TRIM(d.attribute2)")
                + joinAppsTable(apps, "oe_order_headers_all c ON d.header_id = c.header_id")
                + joinAppsTable(apps, "po_requisition_headers_all b ON " + poRequisitionToOeHeaderJoin())
                + joinQualifiedTable(hdr, "roh ON b.requisition_header_id = roh.req_header_id")
                + joinQualifiedTable(dtl, rod + " ON roh.header_id = " + rod + ".header_id")
                + joinIndentShipToHzSite(apps)
                + joinOrderedCustomerHzSite(apps, rod);
    }

    /** HDR ↔ lifting match (SUG tables only). */
    private String hdrToLiftingMatchPredicate(String rohAlias) {
        String hdrCol = OracleErpColumns.RETAIL_HDR_LIFTING_MATCH;
        return "("
                + "TO_CHAR(e.erpindentid) = TO_CHAR("
                + rohAlias
                + "."
                + hdrCol
                + ")\n"
                + "              OR TO_CHAR(e.mobile_order_no) = TO_CHAR("
                + rohAlias
                + "."
                + hdrCol
                + ")\n"
                + "              OR TO_CHAR(e.ordernumber) = TO_CHAR("
                + rohAlias
                + "."
                + hdrCol
                + ")\n"
                + "              OR TO_CHAR(e.erpindentid) = TO_CHAR("
                + rohAlias
                + ".header_id)\n"
                + "              OR TO_CHAR(e.mobile_order_no) = TO_CHAR("
                + rohAlias
                + ".header_id))";
    }

    private static String joinAppsTable(String appsSchema, String tableAndPredicate) {
        return "\n              JOIN " + appsSchema + "." + tableAndPredicate + "\n";
    }

    private static String leftJoinAppsTable(String appsSchema, String tableAndPredicate) {
        return "\n              LEFT JOIN " + appsSchema + "." + tableAndPredicate + "\n";
    }

    /**
     * Ordered shop on each DTL line via {@code rod.customer_code} → HZ account/party/ship-to — not the lifting
     * indent retail name ({@code cus_name}) or OE header ship-to party ({@code hp}).
     */
    private String joinOrderedCustomerHzSite(String appsSchema, String detailAlias) {
        if (!erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            return "";
        }
        String rod = detailAlias == null || detailAlias.isBlank() ? "rod" : detailAlias.trim();
        String siteUse = sanitizeSiteUseCode(erpProperties.getRetailCustomerSiteUseCode());
        return leftJoinAppsTable(
                        appsSchema,
                        "hz_cust_accounts hca_ord ON TRIM(hca_ord.account_number) = TRIM(TO_CHAR("
                                + rod
                                + ".customer_code))")
                + leftJoinAppsTable(appsSchema, "hz_parties hp_ord ON hp_ord.party_id = hca_ord.party_id")
                + leftJoinAppsTable(
                        appsSchema,
                        "hz_cust_acct_sites_all hcas_ord ON hcas_ord.cust_account_id = hca_ord.cust_account_id")
                + leftJoinAppsTable(
                        appsSchema,
                        "hz_cust_site_uses_all csu_ord ON csu_ord.cust_acct_site_id = hcas_ord.cust_acct_site_id"
                                + " AND csu_ord.site_use_code = '"
                                + siteUse
                                + "' AND csu_ord.status = 'A'")
                + leftJoinAppsTable(
                        appsSchema,
                        "hz_party_sites hps_ord ON hps_ord.party_site_id = hcas_ord.party_site_id")
                + leftJoinAppsTable(
                        appsSchema,
                        "hz_locations hl_ord ON hl_ord.location_id = hps_ord.location_id");
    }

    private static String joinQualifiedTable(String qualifiedTable, String aliasAndOn) {
        return "\n              JOIN " + qualifiedTable + " " + aliasAndOn + "\n";
    }

    private String poRequisitionToOeHeaderJoin() {
        if (erpProperties.isRetailPoRequisitionJoinTrim()) {
            return "TRIM(c.orig_sys_document_ref) = TRIM(b.segment1)";
        }
        return "c.orig_sys_document_ref = b.segment1";
    }

    /** OE header ship-to → HZ (GPS/address fallback only when line ship-to is missing). */
    private String joinIndentShipToHzSite(String appsSchema) {
        if (!erpProperties.isRetailCustomerFromHzSiteEnabled() || erpProperties.isRetailOrderSugTablesLink()) {
            return "";
        }
        String siteUse = sanitizeSiteUseCode(erpProperties.getRetailCustomerSiteUseCode());
        return leftJoinAppsTable(
                        appsSchema,
                        "hz_cust_site_uses_all csu ON csu.site_use_id = c.ship_to_org_id AND csu.site_use_code = '"
                                + siteUse
                                + "'")
                + leftJoinAppsTable(
                        appsSchema, "hz_cust_acct_sites_all hcas ON hcas.cust_acct_site_id = csu.cust_acct_site_id")
                + leftJoinAppsTable(
                        appsSchema, "hz_cust_accounts hca ON hca.cust_account_id = hcas.cust_account_id")
                + leftJoinAppsTable(appsSchema, "hz_parties hp ON hp.party_id = hca.party_id");
    }

    private static String sanitizeSiteUseCode(String raw) {
        String code = raw == null || raw.isBlank() ? "SHIP_TO" : raw.trim().toUpperCase();
        if (!code.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalStateException("Invalid Oracle site_use_code: " + code);
        }
        return code;
    }

    /**
     * List/read retail lines by schedule id (binds: scheduleId x2, source, app x2). Supervisor is omitted — the
     * indent already identifies {@code sug_mai_birds_lifting_order} and avoids JWT vs {@code mobileuserempid} mismatches.
     */
    public String whereLiftingScheduleForRetailRead() {
        return whereLiftingSchedulesScopeForRetail(1, false);
    }

    /** Mutations / single-order load (binds: scheduleId x2, source, app x2, supervisor x2). */
    public String whereLiftingScheduleForRetailSecure() {
        return whereLiftingSchedulesScopeForRetail(1, true);
    }

    /** @deprecated use {@link #whereLiftingScheduleForRetailSecure()} */
    public String whereLiftingScheduleForRetail() {
        return whereLiftingScheduleForRetailSecure();
    }

    /**
     * Match any of {@code scheduleCount} lifting schedules (OR of indent match). Binds per id: scheduleId x2, then
     * source, app x2, and optionally supervisor x2.
     */
    public String whereLiftingSchedulesScopeForRetail(int scheduleCount, boolean includeSupervisor) {
        if (scheduleCount < 1) {
            throw new IllegalArgumentException("scheduleCount must be >= 1");
        }
        String indentMatch = ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH.replace("o.", "e.");
        String schedulePredicate;
        if (scheduleCount == 1) {
            schedulePredicate = indentMatch;
        } else {
            StringBuilder or = new StringBuilder();
            for (int i = 0; i < scheduleCount; i++) {
                if (i > 0) {
                    or.append(" OR ");
                }
                or.append("(").append(indentMatch).append(")");
            }
            schedulePredicate = "(" + or + ")";
        }
        String where =
                " WHERE "
                        + schedulePredicate
                        + """
                         AND NVL(e.source, 'X') = ?
                         AND NVL(e.application, ?) = ?
                        """;
        if (includeSupervisor) {
            where += " AND " + LIFTING_SUPERVISOR_MATCH;
        }
        return where;
    }

    /** First column in batch retail list SELECT (alias {@code lifting_schedule_id}). */
    public String selectLiftingScheduleIdColumnExpression() {
        return """
            TO_CHAR(e.erpindentid) AS lifting_schedule_id,
            """;
    }

    /** Full {@code SELECT … FROM} list for one schedule (single-schedule APIs). */
    public String selectDetailListSqlPrefix() {
        return "SELECT " + selectLiftingScheduleIdColumnExpression() + selectDetailCoreColumnList();
    }

    /** Separates SET list from WHERE on retail detail UPDATEs (avoids {@code SYSDATEWHERE}). */
    public static String updateWhereClause() {
        return "\n             WHERE ";
    }

    /** First predicate on {@code rod.order_no} (bind: orderNo). Use after {@code WHERE} on UPDATEs. */
    public static String whereDetailOrderNo() {
        return " TO_CHAR(rod.order_no) = TRIM(?) ";
    }

    /** Extra filter for a single shop line (bind: orderNo). Use after other {@code WHERE} predicates. */
    public static String andDetailOrderNo() {
        return " AND TO_CHAR(rod.order_no) = TRIM(?) ";
    }

    /** Match ordered customer on detail line (bind: customerCode). */
    public String andDetailCustomerCodeFilter() {
        return " AND TO_CHAR(rod.customer_code) = TRIM(?) ";
    }

    /**
     * {@code EXISTS} subquery tying {@code sug_mai_retail_order_dtl} row to a lifting schedule (for UPDATEs).
     * Binds: scheduleId x2, source, app x2, supervisor x2.
     */
    public String existsDetailLinkedToSchedule() {
        String link = "rod_link";
        String correlate = link
                + ".order_no = rod.order_no AND "
                + link
                + ".customer_code = rod.customer_code AND "
                + link
                + ".header_id = rod.header_id ";
        return " EXISTS ( SELECT 1 "
                + fromScheduleToDetailJoin(link)
                + whereLiftingScheduleForRetailSecure()
                + " AND "
                + correlate
                + ") ";
    }

    public String selectDetailCoreColumns() {
        return "SELECT " + selectDetailCoreColumnList();
    }

    /** Column list only (no leading {@code SELECT}). */
    public String selectDetailCoreColumnList() {
        String customerName =
                erpProperties.isRetailCustomerFromHzSiteEnabled()
                        ? "hp_ord.party_name"
                        : "CAST(NULL AS VARCHAR2(240))";
        String address = selectDetailAddressExpression();
        return """
            TO_CHAR(roh.header_id) AS refid,
                   TO_CHAR(rod.order_no) AS order_no,
                   TO_CHAR(rod.customer_code) AS customer_code,
                   NVL(rod.quantity, 0) AS quantity,
                   NVL(rod.weight, 0) AS weight,
                   CAST(NULL AS VARCHAR2(240)) AS item_name,
                   rod.uploaded_flag,
                   rod.message,
                   NVL(roh.rt_branch_code, e.branchcode) AS branch_code,
                   NVL(roh.region, e.regioncode) AS region,
            """
                + customerName
                + " AS customer_name,\n                   CAST(NULL AS VARCHAR2(20)) AS mobile_no,\n                   "
                + address
                + " AS address,\n";
    }

    private String selectDetailAddressExpression() {
        if (!erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            return "CAST(NULL AS VARCHAR2(500))";
        }
        String locationAlias = OracleErpColumns.HZ_LOCATION_ALIAS;
        String locationAddress = hzAcctSiteAddressSql(locationAlias);
        // AR HZ: street address on hz_locations; hcas has ADDRESS_TEXT fallback only.
        String acctSiteText = hzAddressPartAsVarchar("hcas_ord", "ADDRESS_TEXT");
        return "NVL(NULLIF(" + locationAddress + ", ''), NULLIF(" + acctSiteText + ", ''))";
    }

    /** HZ address columns may be CLOB ({@code ADDRESS_TEXT}); cast before {@code TRIM} / {@code ||}. */
    private static String hzAddressPartAsVarchar(String tableAlias, String column) {
        return "TRIM(CAST(" + tableAlias + "." + column + " AS VARCHAR2(4000)))";
    }

    /** Ship-to street address from {@code hz_locations} ({@link OracleErpColumns#HZ_ADDRESS_PARTS}). */
    String hzAcctSiteAddressSql(String siteAlias) {
        String a = siteAlias.trim();
        java.util.List<String> columns = resolveHzAddressColumns();
        if (columns.isEmpty()) {
            return "''";
        }
        StringBuilder expr = new StringBuilder("TRIM(");
        for (int i = 0; i < columns.size(); i++) {
            String col = columns.get(i);
            String part = hzAddressPartAsVarchar(a, col);
            if (i == 0) {
                expr.append("NVL(").append(part).append(", '')");
            } else {
                expr.append(" || NVL2(").append(part).append(", ', ' || ").append(part).append(", '')");
            }
        }
        expr.append(")");
        return expr.toString();
    }

    private java.util.List<String> resolveHzAddressColumns() {
        return java.util.List.of(OracleErpColumns.HZ_ADDRESS_PARTS);
    }

    public String selectDetailGpsColumns() {
        String lat = OracleErpColumns.HZ_SITE_LATITUDE;
        String lon = OracleErpColumns.HZ_SITE_LONGITUDE;
        if (!erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            return """
                   0 AS retailer_latitude,
                   0 AS retailer_longitude
            """;
        }
        String lineLat = hzSiteFlexCoordNumber("hcas_ord", lat);
        String lineLon = hzSiteFlexCoordNumber("hcas_ord", lon);
        String indentLat = hzSiteFlexCoordNumber("hcas", lat);
        String indentLon = hzSiteFlexCoordNumber("hcas", lon);
        if (!erpProperties.isRetailOrderSugTablesLink()) {
            return "                   NVL(NULLIF("
                    + lineLat
                    + ", 0), NVL("
                    + indentLat
                    + ", 0)) AS retailer_latitude,\n                   NVL(NULLIF("
                    + lineLon
                    + ", 0), NVL("
                    + indentLon
                    + ", 0)) AS retailer_longitude\n";
        }
        return "                   NVL("
                + lineLat
                + ", 0) AS retailer_latitude,\n                   NVL("
                + lineLon
                + ", 0) AS retailer_longitude\n";
    }

    /** HZ flex attributes are often VARCHAR2; do not {@code NVL(..., 0)} directly on the column. */
    private static String hzSiteFlexCoordNumber(String tableAlias, String column) {
        String ref = tableAlias + "." + column;
        return "CASE WHEN REGEXP_LIKE(TRIM(TO_CHAR("
                + ref
                + ")), '^-?\\d+(\\.\\d+)?$') THEN TO_NUMBER(TRIM(TO_CHAR("
                + ref
                + "))) END";
    }

    public String selectDelvStatusColumn() {
        return "                   NVL(rod."
                + OracleErpColumns.DELV_STATUS
                + ", 'PENDING') AS delv_status,\n";
    }

    public String selectDelvStartedColumn() {
        return "                   rod." + OracleErpColumns.DELV_START_DT + " AS delivery_started_date,\n";
    }

    /** Per-shop delivery vehicle columns ({@code delv_vehicle_no}, driver, mobile aliases). */
    public String selectDeliveryVehicleColumns() {
        return "                   rod."
                + OracleErpColumns.DELV_VEHICLE_NO
                + " AS delv_vehicle_no,\n                   rod."
                + OracleErpColumns.DELV_DRIVER
                + " AS delv_driver,\n                   rod."
                + OracleErpColumns.DELV_DRIVER_MOB
                + " AS delv_driver_mobile,\n";
    }

    /** {@code SET} clause for per-shop delivery vehicle update (DTL columns + {@code message} mirror tokens). */
    public String buildDeliveryVehicleUpdateSetClause() {
        return "rod."
                + OracleErpColumns.DELV_VEHICLE_NO
                + " = ?,\n                   rod."
                + OracleErpColumns.DELV_DRIVER
                + " = ?,\n                   rod."
                + OracleErpColumns.DELV_DRIVER_MOB
                + " = ?,\n                   rod.message = ?";
    }

    /** Shop OTP on detail line ({@code retail_otp}, {@code otp_expires_at} aliases). */
    public String selectRetailOtpColumns() {
        return "                   rod."
                + OracleErpColumns.RETAIL_OTP
                + " AS retail_otp,\n                   rod."
                + OracleErpColumns.OTP_EXPIRES_AT
                + " AS otp_expires_at,\n                   rod."
                + OracleErpColumns.OTP_VERIFIED
                + " AS otp_verified,\n";
    }

    /** {@code SET} clause for shop OTP resend (message + DTL OTP columns). */
    public String buildReplaceRetailOtpSetClause() {
        return """
                rod.message = ?,
                   rod."""
                + OracleErpColumns.RETAIL_OTP
                + " = ?,\n                   rod."
                + OracleErpColumns.OTP_EXPIRES_AT
                + " = ?\n";
    }

    /** {@code SET} clause for shop OTP verified (message + {@code OTP_VERIFIED}). */
    public String buildMarkRetailOtpVerifiedSetClause() {
        return """
                rod.message = ?,
                   rod."""
                + OracleErpColumns.OTP_VERIFIED
                + " = 'Y'\n";
    }

    /** Appended to delivery-complete UPDATE ({@code OTP_VERIFIED}). */
    public String buildMarkRetailOtpVerifiedSetSuffix() {
        return ",\n                   rod." + OracleErpColumns.OTP_VERIFIED + " = 'Y'";
    }

    public String updateDetailTableName() {
        return OracleErpColumns.SCHEMA_SUG + "." + OracleErpColumns.RETAIL_ORDER_DTL;
    }

    /** {@code UPDATE schema.dtl rod SET } with explicit spacing (safe for string concat). */
    public String buildDetailUpdatePrefix() {
        return "UPDATE " + updateDetailTableName() + " rod\n               SET ";
    }

    public String buildStartDeliverySetClause() {
        return "rod."
                + OracleErpColumns.DELV_STATUS
                + " = 'IN_PROGRESS',\n                   rod."
                + OracleErpColumns.DELV_START_DT
                + " = ?,\n                   rod.message = ?,\n                   rod.uploaded_date = SYSDATE\n";
    }

    public String buildMessageUpdateSetClause() {
        return """
                rod.message = ?,
                   rod.uploaded_date = SYSDATE,
                   rod.weight = NVL(?, rod.weight),
                   rod.quantity = NVL(?, rod.quantity)
            """
                + "\n";
    }

    public String buildLoadOrderMessageStateSql() {
        String statusSelect =
                "NVL(rod." + OracleErpColumns.DELV_STATUS + ", 'PENDING') AS delv_status";
        String dateSelect = "rod." + OracleErpColumns.DELV_START_DT + " AS delivery_started_date";
        return """
            SELECT rod.message, rod.uploaded_flag,
                   """
                + statusSelect
                + ",\n                   "
                + dateSelect
                + "\n"
                + fromScheduleToDetailJoin()
                + whereLiftingScheduleForRetailSecure()
                + andDetailOrderNo();
    }

    public String buildCompleteDetailSetClause() {
        StringBuilder set = new StringBuilder(
                """
                rod.uploaded_flag = 'Y',
                   rod.message = ?,
                   rod.uploaded_date = SYSDATE""");
        set.append(",\n                   rod.").append(OracleErpColumns.DELV_STATUS).append(" = 'COMPLETED'");
        set.append(",\n                   rod.").append(OracleErpColumns.DELV_END_DT).append(" = SYSDATE");
        set.append(buildMarkRetailOtpVerifiedSetSuffix());
        set.append("\n");
        return set.toString();
    }
}
