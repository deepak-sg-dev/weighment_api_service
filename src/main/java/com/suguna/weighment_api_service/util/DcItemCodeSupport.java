package com.suguna.weighment_api_service.util;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.domain.LiftingSchedule;

/** Maps lifting bird/order type to MTL {@code mtl_system_items_b.segment1} for DC HSN/tax. */
public final class DcItemCodeSupport {

    private DcItemCodeSupport() {}

    public static String resolveSegment1(LiftingSchedule schedule, OracleErpProperties erpProperties) {
        if (schedule == null || erpProperties == null) {
            return null;
        }
        String birdType = BirdTypeNormalizer.fromOrderType(schedule.getOperationType());
        if ("CULL".equals(birdType)) {
            return blankToNull(erpProperties.getDcItemCodeCull());
        }
        if ("BROILER".equals(birdType)) {
            String broiler = blankToNull(erpProperties.getDcItemCodeBroiler());
            return broiler != null ? broiler : blankToNull(erpProperties.getDcItemCodeCull());
        }
        return blankToNull(erpProperties.getDcItemCodeCull());
    }

    public static Long parseOrganizationId(String branchId) {
        if (branchId == null || branchId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(branchId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
