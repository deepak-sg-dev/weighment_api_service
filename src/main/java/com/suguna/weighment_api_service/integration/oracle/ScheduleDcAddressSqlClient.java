package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Branch HR location and indent HZ customer address for schedule DC print. */
@Component
public class ScheduleDcAddressSqlClient {

    private static final Logger log = LoggerFactory.getLogger(ScheduleDcAddressSqlClient.class);

    private static final String BRANCH_REG_OFF_SQL =
            """
            SELECT sug_fnd_pkg.get_reg_off_address(org.ledger_id) AS retval
              FROM sug_organization_mv org
             WHERE (TO_CHAR(org.branch_id) = TRIM(?) OR TRIM(org.branch_code) = TRIM(?))
               AND org.ledger_id IS NOT NULL
               AND ROWNUM = 1
            """;

    private static final String FALLBACK_REG_OFF_SQL =
            """
            SELECT sug_fnd_pkg.get_reg_off_address(?) AS retval FROM dual
            """;

    private static final String BRANCH_ADDRESS_SQL =
            """
            SELECT TRIM(LTRIM(
                       TRIM(NVL(hl.address_line_1, ''))
                            || NVL2(NULLIF(TRIM(hl.address_line_2), ''), ', ' || TRIM(hl.address_line_2), '')
                            || NVL2(NULLIF(TRIM(hl.address_line_3), ''), ', ' || TRIM(hl.address_line_3), '')
                            || NVL2(NULLIF(TRIM(hl.town_or_city), ''), ', ' || TRIM(hl.town_or_city), '')
                            || NVL2(NULLIF(TRIM(org.state_name), ''), ', ' || TRIM(org.state_name), '')
                            || NVL2(NULLIF(TRIM(hl.postal_code), ''), ', ' || TRIM(hl.postal_code), ''),
                       ','))
                       AS address,
                   NVL(NULLIF(TRIM(hl.town_or_city), ''), NVL(NULLIF(TRIM(hl.address_line_3), ''), org.location_name))
                       AS city,
                   hl.postal_code AS postal_code,
                   org.state_name AS state_name,
                   NVL(NULLIF(TRIM(org.region_code), ''), TRIM(org.gl_region)) AS region_code,
                   sug_gst_pkg.get_reg_no('GSTN', 'O', org.branch_id) AS gst_no
              FROM sug_organization_mv org
              LEFT JOIN hr_locations_all hl ON hl.location_id = org.hr_location_id
             WHERE (TO_CHAR(org.branch_id) = TRIM(?) OR TRIM(org.branch_code) = TRIM(?))
               AND ROWNUM = 1
            """;

    private static final String BRANCH_ADDRESS_SQL_NO_GST =
            """
            SELECT TRIM(LTRIM(
                       TRIM(NVL(hl.address_line_1, ''))
                            || NVL2(NULLIF(TRIM(hl.address_line_2), ''), ', ' || TRIM(hl.address_line_2), '')
                            || NVL2(NULLIF(TRIM(hl.address_line_3), ''), ', ' || TRIM(hl.address_line_3), '')
                            || NVL2(NULLIF(TRIM(hl.town_or_city), ''), ', ' || TRIM(hl.town_or_city), '')
                            || NVL2(NULLIF(TRIM(org.state_name), ''), ', ' || TRIM(org.state_name), '')
                            || NVL2(NULLIF(TRIM(hl.postal_code), ''), ', ' || TRIM(hl.postal_code), ''),
                       ','))
                       AS address,
                   NVL(NULLIF(TRIM(hl.town_or_city), ''), NVL(NULLIF(TRIM(hl.address_line_3), ''), org.location_name))
                       AS city,
                   hl.postal_code AS postal_code,
                   org.state_name AS state_name,
                   NVL(NULLIF(TRIM(org.region_code), ''), TRIM(org.gl_region)) AS region_code
              FROM sug_organization_mv org
              LEFT JOIN hr_locations_all hl ON hl.location_id = org.hr_location_id
             WHERE (TO_CHAR(org.branch_id) = TRIM(?) OR TRIM(org.branch_code) = TRIM(?))
               AND ROWNUM = 1
            """;

    private final JdbcTemplate jdbcTemplate;
    private final OracleErpProperties erpProperties;
    private final RetailOrderLineSqlSupport retailOrderSql;

    public ScheduleDcAddressSqlClient(
            JdbcTemplate jdbcTemplate, OracleErpProperties erpProperties, RetailOrderLineSqlSupport retailOrderSql) {
        this.jdbcTemplate = jdbcTemplate;
        this.erpProperties = erpProperties;
        this.retailOrderSql = retailOrderSql;
    }

    public Optional<PartyAddress> findBranchAddress(String branchId, String branchCode) {
        if (isBlank(branchId) && isBlank(branchCode)) {
            return Optional.empty();
        }
        String id = branchId == null ? "" : branchId.trim();
        String code = branchCode == null ? "" : branchCode.trim();
        try {
            return firstNonEmpty(queryBranchAddress(BRANCH_ADDRESS_SQL, true, id, code));
        } catch (DataAccessException ex) {
            log.warn("Branch GST lookup failed, loading address without GST: {}", ex.getMessage());
            try {
                return firstNonEmpty(queryBranchAddress(BRANCH_ADDRESS_SQL_NO_GST, false, id, code));
            } catch (DataAccessException retryEx) {
                log.warn("Branch address lookup failed: {}", retryEx.getMessage());
                return Optional.empty();
            }
        }
    }

    private List<PartyAddress> queryBranchAddress(String sql, boolean includeGst, String branchId, String branchCode) {
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new PartyAddress(
                        blankToNull(rs.getString("address")),
                        blankToNull(rs.getString("city")),
                        blankToNull(rs.getString("postal_code")),
                        blankToNull(rs.getString("state_name")),
                        includeGst ? blankToNull(rs.getString("gst_no")) : null,
                        blankToNull(rs.getString("region_code"))),
                branchId,
                branchCode);
    }

    private static Optional<PartyAddress> firstNonEmpty(List<PartyAddress> rows) {
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

    /**
     * Registered office blob for DC print. Uses the branch {@code ledger_id} when present, else
     * {@code app.oracle.erp.dc-registered-office-ledger-id} (typically 2025).
     */
    public Optional<String> findRegisteredOffice(String branchId, String branchCode) {
        try {
            if (!isBlank(branchId) || !isBlank(branchCode)) {
                List<String> rows = jdbcTemplate.query(
                        BRANCH_REG_OFF_SQL,
                        (rs, rowNum) -> blankToNull(rs.getString("retval")),
                        branchId == null ? "" : branchId.trim(),
                        branchCode == null ? "" : branchCode.trim());
                if (!rows.isEmpty() && rows.get(0) != null) {
                    return Optional.of(rows.get(0));
                }
            }
            Long ledgerId = erpProperties.getDcRegisteredOfficeLedgerId();
            if (ledgerId == null) {
                return Optional.empty();
            }
            List<String> fallback = jdbcTemplate.query(
                    FALLBACK_REG_OFF_SQL,
                    (rs, rowNum) -> blankToNull(rs.getString("retval")),
                    ledgerId);
            if (fallback.isEmpty() || fallback.get(0) == null) {
                return Optional.empty();
            }
            return Optional.of(fallback.get(0));
        } catch (DataAccessException ex) {
            log.warn("Registered office lookup failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<PartyAddress> findIndentCustomerAddress(String customerCode, String preferredLocation) {
        if (isBlank(customerCode) || !erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            return Optional.empty();
        }
        String location = blankToNull(preferredLocation);
        String code = customerCode.trim();
        try {
            return firstNonEmpty(queryIndentCustomerAddress(true, location, code));
        } catch (DataAccessException ex) {
            log.warn("Customer GST lookup failed, loading address without GST: {}", ex.getMessage());
            try {
                return firstNonEmpty(queryIndentCustomerAddress(false, location, code));
            } catch (DataAccessException retryEx) {
                log.warn("Indent customer address lookup failed: {}", retryEx.getMessage());
                return Optional.empty();
            }
        }
    }

    private List<PartyAddress> queryIndentCustomerAddress(boolean includeGst, String location, String customerCode) {
        return jdbcTemplate.query(
                retailOrderSql.buildHzIndentCustomerLookupSql(includeGst),
                (rs, rowNum) -> {
                    String address = blankToNull(rs.getString("address"));
                    String city = blankToNull(rs.getString("city"));
                    String postal = blankToNull(rs.getString("postal_code"));
                    String state = blankToNull(rs.getString("state_name"));
                    String gstNo = blankToNull(rs.getString("gstn"));
                    return new PartyAddress(
                            composeAddress(address, city, state, postal), city, postal, state, gstNo, null);
                },
                location,
                location,
                customerCode);
    }

    private static String composeAddress(String street, String city, String state, String postal) {
        if (street == null) {
            return joinParts(city, state, postal);
        }
        String extra = joinParts(state, postal);
        if (extra == null) {
            return street;
        }
        if (street.toUpperCase().contains(extra.toUpperCase())) {
            return street;
        }
        return street + ", " + extra;
    }

    private static String joinParts(String... parts) {
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(", ");
            }
            out.append(part.trim());
        }
        return out.length() == 0 ? null : out.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record PartyAddress(
            String address, String city, String postalCode, String state, String gstNo, String regionCode) {
        boolean isEmpty() {
            return address == null
                    && city == null
                    && postalCode == null
                    && state == null
                    && gstNo == null
                    && regionCode == null;
        }
    }
}
