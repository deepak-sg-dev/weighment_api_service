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
public class DeliveryCustomerAcknowledgementResponse {

    private boolean success;

    private String orderId;

    private AcknowledgementFiles files;

    private String uploadedAt;

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class AcknowledgementFiles {
        private String signature;
        private String customerPhoto;
    }
}
