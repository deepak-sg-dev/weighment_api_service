package com.suguna.weighment_api_service.dto.weighment.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suguna.weighment_api_service.constants.ScheduleStatus;
import org.junit.jupiter.api.Test;

class ScheduleListStatusFilterTest {

    @Test
    void allToken() {
        var filter = ScheduleListStatusFilter.fromQuery("ALL");
        assertTrue(filter.isIncludeAll());
        assertTrue(filter.isValid());
    }

    @Test
    void pendingMapsToAssigned() {
        var filter = ScheduleListStatusFilter.fromQuery("Pending");
        assertFalse(filter.isIncludeAll());
        assertEquals(ScheduleStatus.ASSIGNED, filter.statuses().iterator().next());
    }

    @Test
    void inProgressAliases() {
        assertTrue(ScheduleListStatusFilter.fromQuery("inprogress").isValid());
        assertTrue(ScheduleListStatusFilter.fromQuery("in_progress").isValid());
        assertTrue(ScheduleListStatusFilter.fromQuery("In Progress").isValid());
    }

    @Test
    void completedFilter() {
        var filter = ScheduleListStatusFilter.fromQuery("completed");
        assertEquals(ScheduleStatus.COMPLETED, filter.statuses().iterator().next());
    }

    @Test
    void invalidToken() {
        assertFalse(ScheduleListStatusFilter.fromQuery("bogus").isValid());
    }
}
