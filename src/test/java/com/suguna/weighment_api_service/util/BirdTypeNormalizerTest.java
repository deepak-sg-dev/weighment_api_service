package com.suguna.weighment_api_service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BirdTypeNormalizerTest {

    @Test
    void ordertypeBroiler() {
        assertEquals("BROILER", BirdTypeNormalizer.fromOrderType("BROILER"));
    }

    @Test
    void blankDefaultsBroiler() {
        assertEquals("BROILER", BirdTypeNormalizer.fromOrderType(null));
        assertEquals("BROILER", BirdTypeNormalizer.fromOrderType("  "));
    }

    @Test
    void strainLikeOrdertypeDefaultsBroiler() {
        assertEquals("BROILER", BirdTypeNormalizer.fromOrderType("SUNBRO 205DF"));
    }

    @Test
    void cullFromOrdertype() {
        assertEquals("CULL", BirdTypeNormalizer.fromOrderType("CULL"));
    }
}
