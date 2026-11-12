package com.suguna.weighment_api_service.integration.oracle;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Item GST attributes for DC print via {@code sug_gst_pkg.get_item_info} on {@code mtl_system_items_b}.
 */
@Component
public class ItemGstSqlClient {

    private static final Logger log = LoggerFactory.getLogger(ItemGstSqlClient.class);

    private static final String GST_HSN =
            "sug_gst_pkg.get_item_info('HSN', a.inventory_item_id, a.organization_id)";
    private static final String GST_TAX =
            "sug_gst_pkg.get_item_info('TAXCODE', a.inventory_item_id, a.organization_id)";

    private static final String BY_ORGANIZATION_SQL =
            """
            SELECT %s AS hsn,
                   %s AS tax_code
              FROM mtl_system_items_b a
             WHERE TRIM(a.segment1) = TRIM(?)
               AND a.organization_id = ?
               AND ROWNUM = 1
            """
                    .formatted(GST_HSN, GST_TAX);

    private static final String BY_BRANCH_CODE_SQL =
            """
            SELECT %s AS hsn,
                   %s AS tax_code
              FROM mtl_system_items_b a,
                   sug_organization_mv d
             WHERE d.branch_id = a.organization_id
               AND TRIM(a.segment1) = TRIM(?)
               AND TRIM(d.branch_code) = TRIM(?)
               AND ROWNUM = 1
            """
                    .formatted(GST_HSN, GST_TAX);

    /**
     * When preferred segment1 is missing (broiler strain names are not MTL codes), pick a live-bird item
     * at the branch that already has HSN.
     */
    private static final String BY_BRANCH_LIVE_BIRD_SQL =
            """
            SELECT * FROM (
              SELECT %s AS hsn,
                     %s AS tax_code
                FROM mtl_system_items_b a,
                     sug_organization_mv d
               WHERE d.branch_id = a.organization_id
                 AND TRIM(d.branch_code) = TRIM(?)
                 AND (
                      UPPER(TRIM(a.segment1)) LIKE 'BR%%'
                      OR UPPER(NVL(a.description, 'X')) LIKE '%%BROILER%%'
                      OR UPPER(NVL(a.description, 'X')) LIKE '%%CULL%%'
                      OR (? IS NOT NULL AND UPPER(NVL(a.description, 'X')) LIKE '%%' || UPPER(?) || '%%')
                     )
                 AND %s IS NOT NULL
               ORDER BY CASE
                            WHEN UPPER(TRIM(a.segment1)) LIKE 'BR%%' THEN 0
                            ELSE 1
                        END,
                        a.segment1
            ) WHERE ROWNUM = 1
            """
                    .formatted(GST_HSN, GST_TAX, GST_HSN);

    private final JdbcTemplate jdbcTemplate;

    public ItemGstSqlClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ItemGstInfo> findByItem(String itemSegment1, Long organizationId, String branchCode) {
        return find(itemSegment1, organizationId, branchCode, null);
    }

    public Optional<ItemGstInfo> find(
            String itemSegment1, Long organizationId, String branchCode, String breedOrDescription) {
        String item = blankToNull(itemSegment1);
        String branch = blankToNull(branchCode);
        String breed = blankToNull(breedOrDescription);
        if (item != null && organizationId != null) {
            Optional<ItemGstInfo> byOrg = query(BY_ORGANIZATION_SQL, item, organizationId);
            if (byOrg.isPresent()) {
                return byOrg;
            }
        }
        if (item != null && branch != null) {
            Optional<ItemGstInfo> byBranch = query(BY_BRANCH_CODE_SQL, item, branch);
            if (byBranch.isPresent()) {
                return byBranch;
            }
        }
        if (branch != null) {
            return query(BY_BRANCH_LIVE_BIRD_SQL, branch, breed, breed);
        }
        return Optional.empty();
    }

    private Optional<ItemGstInfo> query(String sql, Object... args) {
        try {
            ItemGstInfo row = jdbcTemplate.query(
                    sql,
                    rs -> {
                        if (!rs.next()) {
                            return null;
                        }
                        return new ItemGstInfo(
                                blankToNull(rs.getString("hsn")), blankToNull(rs.getString("tax_code")));
                    },
                    args);
            if (row == null || (row.hsnCode() == null && row.taxCode() == null)) {
                return Optional.empty();
            }
            return Optional.of(row);
        } catch (DataAccessException ex) {
            log.warn("Item GST lookup failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record ItemGstInfo(String hsnCode, String taxCode) {}
}
