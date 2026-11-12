package com.suguna.weighment_api_service.util;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DcItemCodeSupportTest {

    @Test
    void cullOrderTypeMapsToConfiguredItem() {
        OracleErpProperties props = new OracleErpProperties();
        props.setDcItemCodeCull("BRCULL");
        LiftingSchedule schedule = new LiftingSchedule();
        schedule.setOperationType("CULL");
        assertEquals("BRCULL", DcItemCodeSupport.resolveSegment1(schedule, props));
    }

    @Test
    void broilerFallsBackToCullItemWhenBroilerSegmentUnset() {
        OracleErpProperties props = new OracleErpProperties();
        props.setDcItemCodeCull("BRCULL");
        props.setDcItemCodeBroiler(null);
        LiftingSchedule schedule = new LiftingSchedule();
        schedule.setOperationType("BROILER");
        assertEquals("BRCULL", DcItemCodeSupport.resolveSegment1(schedule, props));
    }

    @Test
    void broilerPrefersDedicatedItemWhenConfigured() {
        OracleErpProperties props = new OracleErpProperties();
        props.setDcItemCodeCull("BRCULL");
        props.setDcItemCodeBroiler("BRBROILER");
        LiftingSchedule schedule = new LiftingSchedule();
        schedule.setOperationType("SUNBRO 205F");
        assertEquals("BRBROILER", DcItemCodeSupport.resolveSegment1(schedule, props));
    }
}
