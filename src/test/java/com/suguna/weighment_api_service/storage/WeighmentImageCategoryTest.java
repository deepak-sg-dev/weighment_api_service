package com.suguna.weighment_api_service.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeighmentImageCategoryTest {

    @Test
    void customerSignatureAliasMapsToCustSign() {
        assertEquals(WeighmentImageCategory.CUST_SIGN, WeighmentImageCategory.parse("CUSTOMER_SIGNATURE"));
        assertEquals(WeighmentImageCategory.CUST_SIGN, WeighmentImageCategory.parse("SIGNATURE"));
    }

    @Test
    void deliverySupervisorAliasMapsToDelSupSign() {
        assertEquals(
                WeighmentImageCategory.DEL_SUP_SIGN,
                WeighmentImageCategory.parse("DELIVERY_SUPERVISOR_SIGNATURE"));
    }

    @Test
    void farmerSignUsesLiftingDomain() {
        WeighmentImageCategory cat = WeighmentImageCategory.parse("FARMER_SIGN");
        assertEquals(WeighmentImageDomain.LIFTING, cat.defaultDomain());
        assertEquals(WeighmentImageKind.FARMER_SIGNATURE, cat.kind());
    }

    @Test
    void legacyLiftingCategoryNamesStillResolve() {
        assertEquals(WeighmentImageCategory.VEHICLE, WeighmentImageCategory.parse("VEHICLE"));
        assertEquals(WeighmentImageCategory.VEHICLE, WeighmentImageCategory.parse("VH"));
        assertEquals(WeighmentImageCategory.COMPLETION, WeighmentImageCategory.parse("COMPLETION"));
        assertEquals(WeighmentImageCategory.COMPLETION, WeighmentImageCategory.parse("CP"));
        assertEquals(WeighmentImageCategory.FARMER_SIGN, WeighmentImageCategory.parse("FARMER_SIGN"));
        assertEquals(WeighmentImageCategory.FARMER_SIGN, WeighmentImageCategory.parse("FARM_SIGN"));
        assertEquals(WeighmentImageCategory.FARMER_SIGN, WeighmentImageCategory.parse("FARMER_SIGNATURE"));
        assertEquals(
                WeighmentImageCategory.LIFT_SUP_SIGN,
                WeighmentImageCategory.parse("LIFTING_SUPERVISOR_SIGNATURE"));
        assertEquals(WeighmentImageCategory.TRADER_SIGN, WeighmentImageCategory.parse("TRADER_SIGN"));
        assertEquals(WeighmentImageCategory.TRADER_SIGN, WeighmentImageCategory.parse("TR_SIGN"));
        assertEquals(WeighmentImageCategory.TRADER_SIGN, WeighmentImageCategory.parse("TRADER_SIGNATURE"));
    }
}
