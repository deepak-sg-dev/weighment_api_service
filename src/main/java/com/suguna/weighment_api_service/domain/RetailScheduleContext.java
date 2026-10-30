package com.suguna.weighment_api_service.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** RETAIL lifting schedule header used to scope {@code sug_mai_ntail_order} lines. */
@Getter
@Builder
@AllArgsConstructor
public final class RetailScheduleContext {

    private final String scheduleId;
    private final String supervisorCode;
    private final String regionCode;
    private final String retailBranchCode;
    private final Instant orderDate;
    private final Long internalPoNumber;
}
