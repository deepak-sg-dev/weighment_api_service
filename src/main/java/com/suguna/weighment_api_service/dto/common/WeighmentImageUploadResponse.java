package com.suguna.weighment_api_service.dto.common;

import java.util.List;
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
public class WeighmentImageUploadResponse {

    private boolean success;

    private String scheduleId;

    /** Set for per-order delivery images (shop, signature, customer). */
    private String orderId;

    /** e.g. VEHICLE, SHOP, OTHER */
    private String category;

    /** Storage root folder: lifting or delivery */
    private String domain;

    private String capturedAt;

    /** First uploaded file (single-upload clients). */
    private UploadedImage photo;

    /** All uploaded files in this request. */
    private List<UploadedImage> photos;

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class UploadedImage {
        private String fileKey;
        private String uploadedAt;
    }
}
