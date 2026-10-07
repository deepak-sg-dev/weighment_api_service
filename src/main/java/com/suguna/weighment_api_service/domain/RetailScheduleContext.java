package com.suguna.weighment_api_service.domain;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/** RETAIL lifting schedule header used to scope {@code sug_mai_ntail_order} lines. */
@Value
@Builder
public class RetailScheduleContext {

    String scheduleId;
    String supervisorCode;
    String regionCode;
    String retailBranchCode;
    Instant orderDate;
    Long internalPoNumber;
}
