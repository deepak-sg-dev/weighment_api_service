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
        return fromScheduleToDetailJoin("rod", true, true, true);
    }

    /**
     * Schedule list / batch: one HZ account + one ship-to (name, address, GPS). Skips indent OE ship-to
     * fallback so list does not fan out.
     */
    public String fromScheduleToDetailJoinForList() {
        return fromScheduleToDetailJoin("rod", true, false, true);
    }

    /**
     * Same lifting→HDR/DTL (and OE/PO when configured) without HZ customer/GPS joins.
     */
    public String fromScheduleToDetailJoinWithoutHz() {
        return fromScheduleToDetailJoin("rod", false, false, true);
    }

    /** Same join chain with a custom detail-line alias (for correlated {@code EXISTS} on UPDATE). */
    public String fromScheduleToDetailJoin(String detailAlias) {
        return fromScheduleToDetailJoin(detailAlias, false, false, false);
    }

    private String fromScheduleToDetailJoin(String detailAlias, boolean includeHzCustomer) {
        return fromScheduleToDetailJoin(detailAlias, includeHzCustomer, includeHzCustomer, true);
    }

    private String fromScheduleToDetailJoin(
            String detailAlias,
            boolean includeHzCustomer,
            boolean includeIndentHzFallback,
            boolean includeNtail) {
        String rod = detailAlias == null || detailAlias.isBlank() ? "rod" : detailAlias.trim();
        String dtl = OracleErpColumns.SCHEMA_SUG + "." + OracleErpColumns.RETAIL_ORDER_DTL;
        String apps = OracleErpColumns.SCHEMA_APPS;
        if (erpProperties.isRetailOrderSugTablesLink()) {
            String join = fromScheduleToHeaderJoin()
                    + joinQualifiedTable(dtl, rod + " ON roh.header_id = " + rod + ".header_id\n");
            if (includeNtail) {
                join += joinNtailOrderedQty(rod);
            }
            if (includeHzCustomer) {
                join += joinOrderedCustomerHzSite(apps, rod);
            }
            return join;
        }
        String join = fromScheduleToHeaderJoin()
                + joinQualifiedTable(dtl, rod + " ON roh.header_id = " + rod + ".header_id");
        if (includeNtail) {
            join += joinNtailOrderedQty(rod);
        }
        if (includeHzCustomer) {
            if (includeIndentHzFallback) {
                join += joinIndentShipToHzSite(apps);
            }
            join += joinOrderedCustomerHzSite(apps, rod);
        }
        return join;
    }

    /**
     * Ordered kg/birds live on {@code sug_mai_ntail_order} (same {@code order_no}). DTL {@code quantity}/{@code
     * weight} are delivery scratch values, not the shop order qty.
     */
    private static String joinNtailOrderedQty(String detailAlias) {
        String rod = detailAlias == null || detailAlias.isBlank() ? "rod" : detailAlias.trim();
        String ntail = OracleErpColumns.SCHEMA_SUG + "." + OracleErpColumns.NTAIL_ORDER;
        return "\n              LEFT JOIN (\n"
                + "                SELECT order_no,\n"
                + "                       TRIM(TO_CHAR(customer_code)) AS customer_code,\n"
                + "                       MAX(trans_date) AS trans_date\n"
                + "                  FROM "
                + ntail
                + "\n"
                + "                 GROUP BY order_no, TRIM(TO_CHAR(customer_code))\n"
                + "              ) n_latest ON n_latest.order_no = "
                + rod
                + ".order_no\n"
                + "               AND n_latest.customer_code = TRIM(TO_CHAR("
                + rod
                + ".customer_code))\n"
                + "              LEFT JOIN "
                + ntail
                + " n ON n.order_no = n_latest.order_no\n"
                + "               AND n.trans_date = n_latest.trans_date\n"
                + "               AND TRIM(TO_CHAR(n.customer_code)) = n_latest.customer_code\n";
    }

    /**
     * Lifting → retail HDR only (no DTL). Used by correlated {@code EXISTS} so an update cannot fan out to sibling
     * shop lines on the same header.
     */
    private String fromScheduleToHeaderJoin() {
        String hdr = OracleErpColumns.SCHEMA_SUG + "." + OracleErpColumns.RETAIL_ORDER_HDR;
        String apps = OracleErpColumns.SCHEMA_APPS;
        if (erpProperties.isRetailOrderSugTablesLink()) {
            return "\n              FROM sug_mai_birds_lifting_order e\n"
                    + joinQualifiedTable(hdr, "roh ON " + hdrToLiftingMatchPredicate("roh"));
        }
        return "\n              FROM sug_mai_birds_lifting_order e\n"
                + joinAppsTable(
                        apps,
                        "oe_order_lines_all d ON TO_CHAR(e.erpindentid) = TRIM(d.attribute2)"
                                + " AND d.line_id = ("
                                + "SELECT MIN(dx.line_id) FROM "
                                + apps
                                + ".oe_order_lines_all dx"
                                + " WHERE TRIM(dx.attribute2) = TO_CHAR(e.erpindentid)"
                                + " AND dx.header_id = d.header_id)")
                + joinAppsTable(apps, "oe_order_headers_all c ON d.header_id = c.header_id")
                + joinAppsTable(apps, "po_requisition_headers_all b ON " + poRequisitionToOeHeaderJoin())
                + joinQualifiedTable(hdr, "roh ON b.requisition_header_id = roh.req_header_id");
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
        return "\n              LEFT JOIN (\n"
                + "                SELECT TRIM(TO_CHAR(account_number)) AS account_number,\n"
                + "                       MIN(cust_account_id) AS cust_account_id\n"
                + "                  FROM "
                + appsSchema
                + ".hz_cust_accounts\n"
                + "                 GROUP BY TRIM(TO_CHAR(account_number))\n"
                + "              ) hca_pick ON hca_pick.account_number = TRIM(TO_CHAR("
                + rod
                + ".customer_code))\n"
                + leftJoinAppsTable(
                        appsSchema, "hz_cust_accounts hca_ord ON hca_ord.cust_account_id = hca_pick.cust_account_id")
                + leftJoinAppsTable(appsSchema, "hz_parties hp_ord ON hp_ord.party_id = hca_ord.party_id")
                + "              LEFT JOIN (\n"
                + "                SELECT hcas_x.cust_account_id, MIN(csu_x.site_use_id) AS site_use_id\n"
                + "                  FROM "
                + appsSchema
                + ".hz_cust_acct_sites_all hcas_x\n"
                + "                  JOIN "
                + appsSchema
                + ".hz_cust_site_uses_all csu_x\n"
                + "                    ON csu_x.cust_acct_site_id = hcas_x.cust_acct_site_id\n"
                + "                   AND csu_x.site_use_code = '"
                + siteUse
                + "' AND csu_x.status = 'A'\n"
                + "                 GROUP BY hcas_x.cust_account_id\n"
                + "              ) csu_pick ON csu_pick.cust_account_id = hca_ord.cust_account_id\n"
                + leftJoinAppsTable(
                        appsSchema, "hz_cust_site_uses_all csu_ord ON csu_ord.site_use_id = csu_pick.site_use_id")
                + leftJoinAppsTable(
                        appsSchema,
                        "hz_cust_acct_sites_all hcas_ord ON hcas_ord.cust_acct_site_id = csu_ord.cust_acct_site_id")
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
        return selectDetailListSqlPrefix(true);
    }

    /**
     * @param includeHzCustomer when false, customer name/address are null (no {@code hp_ord} / location columns).
     */
    public String selectDetailListSqlPrefix(boolean includeHzCustomer) {
        return "SELECT " + selectLiftingScheduleIdColumnExpression() + selectDetailCoreColumnList(includeHzCustomer);
    }

    /** Separates SET list from WHERE on retail detail UPDATEs (avoids {@code SYSDATEWHERE}). */
    public static String updateWhereClause() {
        return "\n             WHERE ";
    }

    /** First predicate on {@code rod.order_no} (bind: orderNo). Use after {@code WHERE} on UPDATEs. */
    public static String whereDetailOrderNo() {
        return " TRIM(TO_CHAR(rod.order_no)) = TRIM(?) ";
    }

    /** Extra filter for a single shop line (bind: orderNo). Use after other {@code WHERE} predicates. */
    public static String andDetailOrderNo() {
        return " AND TRIM(TO_CHAR(rod.order_no)) = TRIM(?) ";
    }

    /** Match ordered customer on detail line (bind: customerCode). */
    public String andDetailCustomerCodeFilter() {
        return " AND TRIM(TO_CHAR(rod.customer_code)) = TRIM(?) ";
    }

    /**
     * {@code EXISTS} tying the outer DTL row's header to this lifting schedule (for UPDATEs).
     * Does not join sibling DTL lines. Binds: scheduleId x2, source, app x2, supervisor x2.
     */
    public String existsDetailLinkedToSchedule() {
        return " EXISTS ( SELECT 1 "
                + fromScheduleToHeaderJoin()
                + whereLiftingScheduleForRetailSecure()
                + " AND roh.header_id = rod.header_id) ";
    }

    public String selectDetailCoreColumns() {
        return "SELECT " + selectDetailCoreColumnList();
    }

    /** Column list only (no leading {@code SELECT}). */
    public String selectDetailCoreColumnList() {
        return selectDetailCoreColumnList(true);
    }

    String selectDetailCoreColumnList(boolean includeHzCustomer) {
        boolean hz = includeHzCustomer && erpProperties.isRetailCustomerFromHzSiteEnabled();
        String customerName = hz ? "hp_ord.party_name" : "CAST(NULL AS VARCHAR2(240))";
        String address = hz ? selectDetailAddressExpression() : "CAST(NULL AS VARCHAR2(500))";
        return """
            TO_CHAR(roh.header_id) AS refid,
                   TO_CHAR(rod.order_no) AS order_no,
                   TO_CHAR(rod.customer_code) AS customer_code,
                   NVL(rod.quantity, NVL(n.quantity, 0)) AS quantity,
                   NVL(n.weight, NVL(rod.weight, 0)) AS weight,
                   rod."""
                + OracleErpColumns.DELV_QUANTITY
                + " AS delivered_quantity,\n                   rod."
                + OracleErpColumns.DELV_WEIGHT
                + """
                 AS delivered_weight,
                   n.item_name,
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

    /**
     * One HZ row per customer code (name, address, GPS) for schedule list. Bind {@code customerCodeCount}
     * {@code TRIM(TO_CHAR(account_number))} values. Does not join lifting/OE.
     */
    public String buildHzCustomerLookupSql(int customerCodeCount) {
        if (customerCodeCount < 1) {
            throw new IllegalArgumentException("customerCodeCount must be >= 1");
        }
        if (!erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            throw new IllegalStateException("HZ customer lookup is disabled");
        }
        String siteUse = sanitizeSiteUseCode(erpProperties.getRetailCustomerSiteUseCode());
        String apps = OracleErpColumns.SCHEMA_APPS;
        String placeholders = String.join(",", java.util.Collections.nCopies(customerCodeCount, "?"));
        String lat = hzSiteFlexCoordNumber("hcas", OracleErpColumns.HZ_SITE_LATITUDE);
        String lon = hzSiteFlexCoordNumber("hcas", OracleErpColumns.HZ_SITE_LONGITUDE);
        String address = hzAcctSiteAddressSql("hl");
        return """
            SELECT customer_code, customer_name, address, retailer_latitude, retailer_longitude, gstn
              FROM (
                SELECT TRIM(TO_CHAR(hca.account_number)) AS customer_code,
                       hp.party_name AS customer_name,
                       NVL(NULLIF(%s, ''), NULLIF(%s, '')) AS address,
                       NVL(%s, 0) AS retailer_latitude,
                       NVL(%s, 0) AS retailer_longitude,
                       sug_gst_pkg.get_reg_no('GSTN', 'CS', hcas.cust_acct_site_id, SYSDATE) AS gstn,
                       ROW_NUMBER() OVER (
                           PARTITION BY TRIM(TO_CHAR(hca.account_number))
                           ORDER BY hca.cust_account_id, csu.site_use_id
                       ) AS rn
                  FROM %s.hz_cust_accounts hca
                  JOIN %s.hz_parties hp ON hp.party_id = hca.party_id
                  LEFT JOIN %s.hz_cust_acct_sites_all hcas
                    ON hcas.cust_account_id = hca.cust_account_id
                  LEFT JOIN %s.hz_cust_site_uses_all csu
                    ON csu.cust_acct_site_id = hcas.cust_acct_site_id
                   AND csu.site_use_code = '%s'
                   AND csu.status = 'A'
                  LEFT JOIN %s.hz_party_sites hps
                    ON hps.party_site_id = hcas.party_site_id
                  LEFT JOIN %s.hz_locations hl
                    ON hl.location_id = hps.location_id
                 WHERE TRIM(TO_CHAR(hca.account_number)) IN (%s)
              )
             WHERE rn = 1
            """
                .formatted(
                        address,
                        hzAddressPartAsVarchar("hcas", "ADDRESS_TEXT"),
                        lat,
                        lon,
                        apps,
                        apps,
                        apps,
                        apps,
                        siteUse,
                        apps,
                        apps,
                        placeholders);
    }

    /**
     * One HZ ship-to for the lifting indent customer. Binds: preferredLocation, preferredLocation,
     * accountNumber. Prefers {@code hz_locations.address1} matching {@code CUS_LOCATION}.
     */
    public String buildHzIndentCustomerLookupSql() {
        return buildHzIndentCustomerLookupSql(true);
    }

    public String buildHzIndentCustomerLookupSql(boolean includeGst) {
        if (!erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            throw new IllegalStateException("HZ customer lookup is disabled");
        }
        String siteUse = sanitizeSiteUseCode(erpProperties.getRetailCustomerSiteUseCode());
        String apps = OracleErpColumns.SCHEMA_APPS;
        String lat = hzSiteFlexCoordNumber("hcas", OracleErpColumns.HZ_SITE_LATITUDE);
        String lon = hzSiteFlexCoordNumber("hcas", OracleErpColumns.HZ_SITE_LONGITUDE);
        String address = hzAcctSiteAddressSql("hl");
        String gstn =
                includeGst
                        ? "sug_gst_pkg.get_reg_no('GSTN', 'CS', hcas.cust_acct_site_id, SYSDATE)"
                        : "CAST(NULL AS VARCHAR2(64))";
        return """
            SELECT customer_code, customer_name, address, city, postal_code, state_name,
                   retailer_latitude, retailer_longitude, gstn
              FROM (
                SELECT TRIM(TO_CHAR(hca.account_number)) AS customer_code,
                       hp.party_name AS customer_name,
                       NVL(NULLIF(%s, ''), NULLIF(%s, '')) AS address,
                       hl.city AS city,
                       hl.postal_code AS postal_code,
                       hl.state AS state_name,
                       NVL(%s, 0) AS retailer_latitude,
                       NVL(%s, 0) AS retailer_longitude,
                       %s AS gstn,
                       ROW_NUMBER() OVER (
                           PARTITION BY TRIM(TO_CHAR(hca.account_number))
                           ORDER BY CASE
                               WHEN ? IS NULL THEN 1
                               WHEN UPPER(TRIM(hl.address1)) = UPPER(TRIM(?)) THEN 0
                               ELSE 1
                           END,
                           hca.cust_account_id,
                           csu.site_use_id
                       ) AS rn
                  FROM %s.hz_cust_accounts hca
                  JOIN %s.hz_parties hp ON hp.party_id = hca.party_id
                  LEFT JOIN %s.hz_cust_acct_sites_all hcas
                    ON hcas.cust_account_id = hca.cust_account_id
                  LEFT JOIN %s.hz_cust_site_uses_all csu
                    ON csu.cust_acct_site_id = hcas.cust_acct_site_id
                   AND csu.site_use_code = '%s'
                   AND csu.status = 'A'
                  LEFT JOIN %s.hz_party_sites hps
                    ON hps.party_site_id = hcas.party_site_id
                  LEFT JOIN %s.hz_locations hl
                    ON hl.location_id = hps.location_id
                 WHERE TRIM(TO_CHAR(hca.account_number)) = TRIM(?)
              )
             WHERE rn = 1
            """
                .formatted(
                        address,
                        hzAddressPartAsVarchar("hcas", "ADDRESS_TEXT"),
                        lat,
                        lon,
                        gstn,
                        apps,
                        apps,
                        apps,
                        apps,
                        siteUse,
                        apps,
                        apps);
    }

    /** List APIs omit HZ GPS (zeros). */
    public String selectDetailGpsStubColumns() {
        return """
                   0 AS retailer_latitude,
                   0 AS retailer_longitude,
                   CAST(NULL AS VARCHAR2(64)) AS customer_gstn
            """;
    }

    public String selectDetailGpsColumns() {
        return selectDetailGpsColumns(true);
    }

    /** List GPS from the ordered customer ship-to only (no indent {@code hcas} fallback). */
    public String selectDetailGpsColumnsForList() {
        return selectDetailGpsColumns(false);
    }

    private String selectDetailGpsColumns(boolean includeIndentFallback) {
        String lat = OracleErpColumns.HZ_SITE_LATITUDE;
        String lon = OracleErpColumns.HZ_SITE_LONGITUDE;
        if (!erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            return """
                   0 AS retailer_latitude,
                   0 AS retailer_longitude,
                   CAST(NULL AS VARCHAR2(64)) AS customer_gstn
            """;
        }
        String lineLat = hzSiteFlexCoordNumber("hcas_ord", lat);
        String lineLon = hzSiteFlexCoordNumber("hcas_ord", lon);
        if (!includeIndentFallback || erpProperties.isRetailOrderSugTablesLink()) {
            return "                   NVL("
                    + lineLat
                    + ", 0) AS retailer_latitude,\n                   NVL("
                    + lineLon
                    + ", 0) AS retailer_longitude,\n"
                    + selectOrderedCustomerGstnColumn();
        }
        String indentLat = hzSiteFlexCoordNumber("hcas", lat);
        String indentLon = hzSiteFlexCoordNumber("hcas", lon);
        return "                   NVL(NULLIF("
                + lineLat
                + ", 0), NVL("
                + indentLat
                + ", 0)) AS retailer_latitude,\n                   NVL(NULLIF("
                + lineLon
                + ", 0), NVL("
                + indentLon
                + ", 0)) AS retailer_longitude,\n"
                + selectOrderedCustomerGstnColumn();
    }

    /**
     * Shop GSTIN for the ordered HZ ship-to ({@code sug_gst_pkg.get_reg_no('GSTN','CS', cust_acct_site_id, SYSDATE)}).
     */
    private String selectOrderedCustomerGstnColumn() {
        return "                   CASE WHEN hcas_ord.cust_acct_site_id IS NULL THEN CAST(NULL AS VARCHAR2(64))"
                + " ELSE sug_gst_pkg.get_reg_no('GSTN', 'CS', hcas_ord.cust_acct_site_id, SYSDATE) END"
                + " AS customer_gstn\n";
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

    /** Sets delivered kg/birds only; does not change requested {@code quantity}/{@code weight}. */
    public String buildDeliveredQtyUpdateSetClause() {
        return "rod."
                + OracleErpColumns.DELV_WEIGHT
                + " = NVL(?, rod."
                + OracleErpColumns.DELV_WEIGHT
                + "),\n                   rod."
                + OracleErpColumns.DELV_QUANTITY
                + " = NVL(?, rod."
                + OracleErpColumns.DELV_QUANTITY
                + "),\n                   rod.uploaded_date = SYSDATE\n";
    }

    public String buildMessageUpdateSetClause() {
        return """
                rod.message = ?,
                   rod.uploaded_date = SYSDATE,
                   rod."""
                + OracleErpColumns.DELV_WEIGHT
                + " = NVL(?, rod."
                + OracleErpColumns.DELV_WEIGHT
                + "),\n                   rod."
                + OracleErpColumns.DELV_QUANTITY
                + " = NVL(?, rod."
                + OracleErpColumns.DELV_QUANTITY
                + ")\n";
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
