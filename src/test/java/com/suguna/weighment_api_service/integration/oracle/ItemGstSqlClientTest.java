package com.suguna.weighment_api_service.integration.oracle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemGstSqlClientTest {

    @Test
    void lookupSqlUsesGstPackageHsnAndTaxCode() {
        String byOrg =
                """
                SELECT sug_gst_pkg.get_item_info('HSN', a.inventory_item_id, a.organization_id) AS hsn,
                       sug_gst_pkg.get_item_info('TAXCODE', a.inventory_item_id, a.organization_id) AS tax_code
                  FROM mtl_system_items_b a
                 WHERE TRIM(a.segment1) = TRIM(?)
                   AND a.organization_id = ?
                """
                        .replaceAll("\\s+", " ")
                        .toUpperCase();
        assertTrue(byOrg.contains("GET_ITEM_INFO('HSN'"), byOrg);
        assertTrue(byOrg.contains("GET_ITEM_INFO('TAXCODE'"), byOrg);
        assertTrue(byOrg.contains("MTL_SYSTEM_ITEMS_B"), byOrg);
        assertTrue(byOrg.contains("SUG_GST_PKG"), byOrg);
    }
}
