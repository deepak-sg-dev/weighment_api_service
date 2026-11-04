package com.suguna.weighment_api_service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BranchNameSupportTest {

    @Test
    void deriveShortName_stripsCodePrefixAndBroilerBranchSuffix() {
        assertEquals(
                "INDAPUR",
                BranchNameSupport.deriveShortName("M47:INDAPUR BROILER BRANCH-MH"));
    }

    @Test
    void deriveShortName_withoutColon_stripsBroilerSuffixOnly() {
        assertEquals(
                "MR1 PUNE RT",
                BranchNameSupport.deriveShortName("MR1 PUNE RT BROILER BRANCH-MH"));
    }

    @Test
    void deriveShortName_blankReturnsNull() {
        assertNull(BranchNameSupport.deriveShortName(null));
        assertNull(BranchNameSupport.deriveShortName("   "));
    }
}
