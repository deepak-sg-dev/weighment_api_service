package com.suguna.weighment_api_service.integration.oracle;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Item GST attributes for DC print via {@code sug_gst_pkg.get_item_info}.
 *
 * <p>Resolve {@code inventory_item_id} first, then call the GST package once for that row. Never use
 * {@code get_item_info} in a WHERE/ORDER BY over {@code mtl_system_items_b} (that can run for a minute).
 */
@Component
public class ItemGstSqlClient {

    private static final Logger log = LoggerFactory.getLogger(ItemGstSqlClient.class);

    private static final String RESOLVE_BY_ORGANIZATION_SQL =
            """
            SELECT a.inventory_item_id, a.organization_id
              FROM mtl_system_items_b a
             WHERE TRIM(a.segment1) = TRIM(?)
               AND a.organization_id = ?
               AND ROWNUM = 1
            """;

    private static final String RESOLVE_BY_BRANCH_SQL =
            """
            SELECT a.inventory_item_id, a.organization_id
              FROM mtl_system_items_b a
              JOIN sug_organization_mv d ON d.branch_id = a.organization_id
             WHERE TRIM(a.segment1) = TRIM(?)
               AND TRIM(d.branch_code) = TRIM(?)
               AND ROWNUM = 1
            """;

    private static final String RESOLVE_BR_PREFIX_BY_BRANCH_SQL =
            """
            SELECT inventory_item_id, organization_id
              FROM (
                SELECT a.inventory_item_id, a.organization_id
                  FROM mtl_system_items_b a
                  JOIN sug_organization_mv d ON d.branch_id = a.organization_id
                 WHERE TRIM(d.branch_code) = TRIM(?)
                   AND UPPER(TRIM(a.segment1)) LIKE 'BR%'
                 ORDER BY a.segment1
              )
             WHERE ROWNUM = 1
            """;

    private static final String ITEM_INFO_SQL =
            """
            SELECT sug_gst_pkg.get_item_info('HSN', ?, ?) AS hsn,
                   sug_gst_pkg.get_item_info('TAXCODE', ?, ?) AS tax_code
              FROM dual
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ConcurrentHashMap<String, Optional<ItemGstInfo>> cache = new ConcurrentHashMap<>();

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
        String cacheKey = (item == null ? "" : item)
                + "|"
                + (organizationId == null ? "" : organizationId)
                + "|"
                + (branch == null ? "" : branch);
        return cache.computeIfAbsent(cacheKey, key -> lookup(item, organizationId, branch));
    }

    private Optional<ItemGstInfo> lookup(String item, Long organizationId, String branch) {
        Optional<ItemKey> resolved = Optional.empty();
        if (item != null && organizationId != null) {
            resolved = resolveItem(RESOLVE_BY_ORGANIZATION_SQL, item, organizationId);
        }
        if (resolved.isEmpty() && item != null && branch != null) {
            resolved = resolveItem(RESOLVE_BY_BRANCH_SQL, item, branch);
        }
        if (resolved.isEmpty() && branch != null) {
            resolved = resolveItem(RESOLVE_BR_PREFIX_BY_BRANCH_SQL, branch);
        }
        return resolved.flatMap(this::queryItemInfo);
    }

    private Optional<ItemKey> resolveItem(String sql, Object... args) {
        try {
            List<ItemKey> rows = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> new ItemKey(rs.getLong("inventory_item_id"), rs.getLong("organization_id")),
                    args);
            return rows.stream().findFirst();
        } catch (DataAccessException ex) {
            log.warn("MTL item resolve failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<ItemGstInfo> queryItemInfo(ItemKey item) {
        try {
            ItemGstInfo row = jdbcTemplate.query(
                    ITEM_INFO_SQL,
                    rs -> {
                        if (!rs.next()) {
                            return null;
                        }
                        return new ItemGstInfo(
                                blankToNull(rs.getString("hsn")), blankToNull(rs.getString("tax_code")));
                    },
                    item.inventoryItemId(),
                    item.organizationId(),
                    item.inventoryItemId(),
                    item.organizationId());
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

    private record ItemKey(long inventoryItemId, long organizationId) {}

    public record ItemGstInfo(String hsnCode, String taxCode) {}
}
