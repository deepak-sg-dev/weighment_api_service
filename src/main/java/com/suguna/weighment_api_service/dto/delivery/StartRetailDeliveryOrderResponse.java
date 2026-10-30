package com.suguna.weighment_api_service.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class StartRetailDeliveryOrderResponse {

    private boolean success;
    private String scheduleId;
    private String orderId;
    /** Same as {@link #deliveryStatus}. */
    private String status;
    /** Per-order delivery: PENDING, IN_PROGRESS, COMPLETED. */
    private String deliveryStatus;
    private String startedAt;
    private String message;
}
