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
public class DeliveryShopPhotoResponse {

    private boolean success;

    private String orderId;

    private UploadedFile photo;

    private String uploadedAt;

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class UploadedFile {
        private String fileKey;
    }
}
