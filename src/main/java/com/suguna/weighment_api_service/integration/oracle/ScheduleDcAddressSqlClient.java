package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Branch HR location and indent HZ customer address for schedule DC print. */
@Component
public class ScheduleDcAddressSqlClient {

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
                   org.state_name AS state_name
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
        List<PartyAddress> rows = jdbcTemplate.query(
                BRANCH_ADDRESS_SQL,
                (rs, rowNum) -> new PartyAddress(
                        blankToNull(rs.getString("address")),
                        blankToNull(rs.getString("city")),
                        blankToNull(rs.getString("postal_code")),
                        blankToNull(rs.getString("state_name"))),
                branchId == null ? "" : branchId.trim(),
                branchCode == null ? "" : branchCode.trim());
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

    public Optional<PartyAddress> findIndentCustomerAddress(String customerCode, String preferredLocation) {
        if (isBlank(customerCode) || !erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            return Optional.empty();
        }
        String location = blankToNull(preferredLocation);
        List<PartyAddress> rows = jdbcTemplate.query(
                retailOrderSql.buildHzIndentCustomerLookupSql(),
                (rs, rowNum) -> {
                    String address = blankToNull(rs.getString("address"));
                    String city = blankToNull(rs.getString("city"));
                    String postal = blankToNull(rs.getString("postal_code"));
                    String state = blankToNull(rs.getString("state_name"));
                    return new PartyAddress(composeAddress(address, city, state, postal), city, postal, state);
                },
                location,
                location,
                customerCode.trim());
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
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

    public record PartyAddress(String address, String city, String postalCode, String state) {
        boolean isEmpty() {
            return address == null && city == null && postalCode == null && state == null;
        }
    }
}
