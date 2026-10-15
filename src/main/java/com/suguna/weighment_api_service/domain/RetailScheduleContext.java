package com.suguna.weighment_api_service.domain;

import java.time.Instant;

/** RETAIL lifting schedule header used to scope {@code sug_mai_ntail_order} lines. */
public final class RetailScheduleContext {

    private final String scheduleId;
    private final String supervisorCode;
    private final String regionCode;
    private final String retailBranchCode;
    private final Instant orderDate;
    private final Long internalPoNumber;

    private RetailScheduleContext(Builder builder) {
        this.scheduleId = builder.scheduleId;
        this.supervisorCode = builder.supervisorCode;
        this.regionCode = builder.regionCode;
        this.retailBranchCode = builder.retailBranchCode;
        this.orderDate = builder.orderDate;
        this.internalPoNumber = builder.internalPoNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public String getSupervisorCode() {
        return supervisorCode;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getRetailBranchCode() {
        return retailBranchCode;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public Long getInternalPoNumber() {
        return internalPoNumber;
    }

    public static final class Builder {
        private String scheduleId;
        private String supervisorCode;
        private String regionCode;
        private String retailBranchCode;
        private Instant orderDate;
        private Long internalPoNumber;

        public Builder scheduleId(String scheduleId) {
            this.scheduleId = scheduleId;
            return this;
        }

        public Builder supervisorCode(String supervisorCode) {
            this.supervisorCode = supervisorCode;
            return this;
        }

        public Builder regionCode(String regionCode) {
            this.regionCode = regionCode;
            return this;
        }

        public Builder retailBranchCode(String retailBranchCode) {
            this.retailBranchCode = retailBranchCode;
            return this;
        }

        public Builder orderDate(Instant orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder internalPoNumber(Long internalPoNumber) {
            this.internalPoNumber = internalPoNumber;
            return this;
        }

        public RetailScheduleContext build() {
            return new RetailScheduleContext(this);
        }
    }
}
