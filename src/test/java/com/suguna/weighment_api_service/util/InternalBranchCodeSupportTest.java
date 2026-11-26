package com.suguna.weighment_api_service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class InternalBranchCodeSupportTest {

    @Test
    void stripsLabelAfterColon() {
        assertEquals("M47", InternalBranchCodeSupport.fromLabel("M47:INDAPUR BROILER BRANCH-MH"));
        assertEquals("MR1", InternalBranchCodeSupport.fromLabel("MR1:PUNE RETAIL"));
    }

    @Test
    void keepsBareCode() {
        assertEquals("M47", InternalBranchCodeSupport.fromLabel("M47"));
        assertNull(InternalBranchCodeSupport.fromLabel(" "));
    }
}
