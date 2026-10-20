package com.suguna.weighment_api_service.dto.delivery;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SubmitDeliveryWeighmentResponse {

    private boolean success;

    private String message;

    private String scheduleId;

    private String orderId;

    /** e.g. RECEIVED */
    private String status;

    private String receivedAt;
}
