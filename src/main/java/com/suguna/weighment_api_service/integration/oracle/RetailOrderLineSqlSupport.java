package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import org.springframework.stereotype.Component;

/**
 * Links a retail lifting schedule ({@code sug_mai_birds_lifting_order.erpindentid}) to shop delivery lines on
 * {@code sug_mai_retail_order_dtl} via OE order lines ({@code attribute2} = schedule) and retail header
 * ({@code req_header_id}).
 *
 * <p>Customer display and {@code retailerId} use Oracle HZ ship-to site on the OE order when
 * {@code app.oracle.erp.retail-customer-from-hz-site-enabled=true}.
 */
@Component
public class RetailOrderLineSqlSupport {

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
        String apps = erpProperties.getOracleAppsSchema();
        String sug = erpProperties.getOracleSugSchema();
        String hdr = sug + "." + erpProperties.getRetailOrderHdrTable();
        String dtl = sug + "." + erpProperties.getRetailOrderDtlTable();
        return "\n              FROM sug_mai_birds_lifting_order e\n"
                + joinAppsTable(apps, "oe_order_lines_all d ON TO_CHAR(e.erpindentid) = TRIM(d.attribute2)")
                + joinAppsTable(apps, "oe_order_headers_all c ON d.header_id = c.header_id")
                + joinAppsTable(apps, "po_requisition_headers_all b ON c.orig_sys_document_ref = b.segment1")
                + joinQualifiedTable(hdr, "roh ON b.requisition_header_id = roh.req_header_id")
                + joinQualifiedTable(dtl, rod + " ON roh.header_id = " + rod + ".header_id")
                + joinHzCustomerAtSite(apps);
    }

    private static String joinAppsTable(String appsSchema, String tableAndPredicate) {
        return "\n              JOIN " + appsSchema + "." + tableAndPredicate + "\n";
    }

    private static String joinQualifiedTable(String qualifiedTable, String aliasAndOn) {
        return "\n              JOIN " + qualifiedTable + " " + aliasAndOn + "\n";
    }

    /** OE header ship-to → HZ account / party / site location (inner join when HZ mode is on). */
    private String joinHzCustomerAtSite(String appsSchema) {
        if (!erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            return "";
        }
        String siteUse = sanitizeSiteUseCode(erpProperties.getRetailCustomerSiteUseCode());
        return joinAppsTable(
                        appsSchema,
                        "hz_cust_site_uses_all csu ON csu.site_use_id = c.ship_to_org_id AND csu.site_use_code = '"
                                + siteUse
                                + "'")
                + joinAppsTable(appsSchema, "hz_cust_acct_sites_all hcas ON hcas.cust_acct_site_id = csu.cust_acct_site_id")
                + joinAppsTable(appsSchema, "hz_cust_accounts hca ON hca.cust_account_id = hcas.cust_account_id")
                + joinAppsTable(appsSchema, "hz_parties hp ON hp.party_id = hca.party_id");
    }

    private static String sanitizeSiteUseCode(String raw) {
        String code = raw == null || raw.isBlank() ? "SHIP_TO" : raw.trim().toUpperCase();
        if (!code.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalStateException("Invalid Oracle site_use_code: " + code);
        }
        return code;
    }

    /** Filter lifting row + application/source (binds: scheduleId x2, source, app x2, supervisor x2). */
    public String whereLiftingScheduleForRetail() {
        return " WHERE "
                + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH.replace("o.", "e.")
                + """
                 AND NVL(e.source, 'X') = ?
                 AND NVL(e.application, ?) = ?
                 AND """
                + LIFTING_SUPERVISOR_MATCH;
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

    /**
     * Match retailer on detail line (bind: customerCode). Empty when HZ site mode is on — retailer is validated in
     * service layer against {@code hz_cust_accounts.account_number}; updates scope by {@code order_no} + schedule.
     */
    public String andDetailCustomerCodeFilter() {
        if (erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            return "";
        }
        return " AND TO_CHAR(rod.customer_code) = TRIM(?) ";
    }

    /**
     * {@code EXISTS} subquery tying {@code sug_mai_retail_order_dtl} row to a lifting schedule (for UPDATEs).
     * Binds: scheduleId x2, source, app x2, supervisor x2.
     */
    public String existsDetailLinkedToSchedule() {
        String link = "rod_link";
        return " EXISTS ( SELECT 1 "
                + fromScheduleToDetailJoin(link)
                + whereLiftingScheduleForRetail()
                + " AND "
                + link
                + ".order_no = rod.order_no AND "
                + link
                + ".customer_code = rod.customer_code AND "
                + link
                + ".header_id = rod.header_id ) ";
    }

    public String selectDetailCoreColumns() {
        if (erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            return """
            SELECT TO_CHAR(roh.header_id) AS refid,
                   TO_CHAR(rod.order_no) AS order_no,
                   TO_CHAR(hca.account_number) AS customer_code,
                   NVL(rod.quantity, 0) AS quantity,
                   NVL(rod.weight, 0) AS weight,
                   CAST(NULL AS VARCHAR2(240)) AS item_name,
                   rod.uploaded_flag,
                   rod.message,
                   e.branchcode AS branch_code,
                   e.regioncode AS region,
                   hp.party_name AS customer_name,
                   CAST(NULL AS VARCHAR2(20)) AS mobile_no,
                   csu.location AS address,
            """;
        }
        return """
            SELECT TO_CHAR(roh.header_id) AS refid,
                   TO_CHAR(rod.order_no) AS order_no,
                   TO_CHAR(rod.customer_code) AS customer_code,
                   NVL(rod.quantity, 0) AS quantity,
                   NVL(rod.weight, 0) AS weight,
                   rod.item_name,
                   rod.uploaded_flag,
                   rod.message,
                   NVL(rod.branch_code, e.branchcode) AS branch_code,
                   NVL(rod.region, e.regioncode) AS region,
                   rod.customer_name,
                   rod.mobile_no,
                   rod.address,
            """;
    }

    public String selectDetailGpsColumns() {
        if (!erpProperties.isRetailCustomerGpsEnabled()) {
            return """
                   0 AS retailer_latitude,
                   0 AS retailer_longitude
            """;
        }
        String lat = RetailDeliverySqlClient.sanitizeOracleColumnName(
                erpProperties.getRetailCustomerLatitudeColumn(), "ATTRIBUTE11");
        String lon = RetailDeliverySqlClient.sanitizeOracleColumnName(
                erpProperties.getRetailCustomerLongitudeColumn(), "ATTRIBUTE10");
        if (erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            return "                   NVL(hcas."
                    + lat
                    + ", 0) AS retailer_latitude,\n                   NVL(hcas."
                    + lon
                    + ", 0) AS retailer_longitude\n";
        }
        return "                   NVL(rod." + lat + ", 0) AS retailer_latitude,\n                   NVL(rod." + lon
                + ", 0) AS retailer_longitude\n";
    }

    public String selectDelvStatusColumn() {
        if (!erpProperties.isRetailOrderDeliveryStatusEnabled()) {
            return """
                   CAST(NULL AS VARCHAR2(32)) AS delv_status,
            """;
        }
        String col = RetailDeliverySqlClient.sanitizeOracleColumnName(
                erpProperties.getRetailOrderDeliveryStatusColumn(), "DELV_STATUS");
        return "                   NVL(rod." + col + ", 'PENDING') AS delv_status,\n";
    }

    public String selectDelvStartedColumn() {
        if (!erpProperties.isRetailOrderDeliveryStartEnabled()) {
            return """
                   CAST(NULL AS DATE) AS delivery_started_date,
            """;
        }
        String col = RetailDeliverySqlClient.sanitizeOracleColumnName(
                erpProperties.getRetailOrderDeliveryStartedDateColumn(), "DELV_START_DT");
        return "                   rod." + col + " AS delivery_started_date,\n";
    }

    /** Shop OTP on detail line ({@code retail_otp}, {@code otp_expires_at} aliases). */
    public String selectRetailOtpColumns() {
        if (!erpProperties.isRetailOrderOtpUseDtlColumn()) {
            return """
                   CAST(NULL AS VARCHAR2(8)) AS retail_otp,
                   CAST(NULL AS DATE) AS otp_expires_at,
            """;
        }
        String otpCol = RetailDeliverySqlClient.sanitizeOracleColumnName(
                erpProperties.getRetailOrderOtpColumn(), "RETAIL_OTP");
        String expiresCol = RetailDeliverySqlClient.sanitizeOracleColumnName(
                erpProperties.getRetailOrderOtpExpiresColumn(), "OTP_EXPIRES_AT");
        return "                   rod." + otpCol + " AS retail_otp,\n                   rod." + expiresCol
                + " AS otp_expires_at,\n";
    }

    /** Appended to delivery-complete UPDATE when DTL OTP columns exist. */
    public String buildMarkRetailOtpVerifiedSetSuffix() {
        if (!erpProperties.isRetailOrderOtpUseDtlColumn()) {
            return "";
        }
        String verifiedCol = RetailDeliverySqlClient.sanitizeOracleColumnName(
                erpProperties.getRetailOrderOtpVerifiedColumn(), "OTP_VERIFIED");
        return ",\n                   rod." + verifiedCol + " = 'Y'";
    }

    public String updateDetailTableName() {
        return erpProperties.getOracleSugSchema() + "." + erpProperties.getRetailOrderDtlTable();
    }

    /** {@code UPDATE schema.dtl rod SET } with explicit spacing (safe for string concat). */
    public String buildDetailUpdatePrefix() {
        return "UPDATE " + updateDetailTableName() + " rod\n               SET ";
    }

    public String buildStartDeliverySetClause() {
        StringBuilder set = new StringBuilder();
        if (erpProperties.isRetailOrderDeliveryStatusEnabled()) {
            String statusCol = RetailDeliverySqlClient.sanitizeOracleColumnName(
                    erpProperties.getRetailOrderDeliveryStatusColumn(), "DELV_STATUS");
            set.append("rod.").append(statusCol).append(" = 'IN_PROGRESS',\n                   ");
        }
        if (erpProperties.isRetailOrderDeliveryStartEnabled()) {
            String startCol = RetailDeliverySqlClient.sanitizeOracleColumnName(
                    erpProperties.getRetailOrderDeliveryStartedDateColumn(), "DELV_START_DT");
            set.append("rod.").append(startCol).append(" = ?,\n                   ");
        }
        set.append("rod.message = ?,\n                   rod.uploaded_date = SYSDATE\n");
        return set.toString();
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
        String statusSelect = erpProperties.isRetailOrderDeliveryStatusEnabled()
                ? "NVL(rod."
                        + RetailDeliverySqlClient.sanitizeOracleColumnName(
                                erpProperties.getRetailOrderDeliveryStatusColumn(), "DELV_STATUS")
                        + ", 'PENDING') AS delv_status"
                : "CAST(NULL AS VARCHAR2(32)) AS delv_status";
        String dateSelect = erpProperties.isRetailOrderDeliveryStartEnabled()
                ? "rod."
                        + RetailDeliverySqlClient.sanitizeOracleColumnName(
                                erpProperties.getRetailOrderDeliveryStartedDateColumn(), "DELV_START_DT")
                        + " AS delivery_started_date"
                : "CAST(NULL AS DATE) AS delivery_started_date";
        return """
            SELECT rod.message, rod.uploaded_flag,
                   """
                + statusSelect
                + ",\n                   "
                + dateSelect
                + "\n"
                + fromScheduleToDetailJoin()
                + whereLiftingScheduleForRetail()
                + andDetailOrderNo();
    }

    public String buildCompleteDetailSetClause() {
        StringBuilder set = new StringBuilder(
                """
                rod.uploaded_flag = 'Y',
                   rod.message = ?,
                   rod.uploaded_date = SYSDATE""");
        if (erpProperties.isRetailOrderDeliveryStatusEnabled()) {
            String statusCol = RetailDeliverySqlClient.sanitizeOracleColumnName(
                    erpProperties.getRetailOrderDeliveryStatusColumn(), "DELV_STATUS");
            set.append(",\n                   rod.").append(statusCol).append(" = 'COMPLETED'");
        }
        if (erpProperties.isRetailOrderDeliveryStartEnabled()) {
            String endCol = RetailDeliverySqlClient.sanitizeOracleColumnName(
                    erpProperties.getRetailOrderDeliveryCompletedDateColumn(), "DELV_END_DT");
            set.append(",\n                   rod.").append(endCol).append(" = SYSDATE");
        }
        set.append(buildMarkRetailOtpVerifiedSetSuffix());
        set.append("\n");
        return set.toString();
    }
}
