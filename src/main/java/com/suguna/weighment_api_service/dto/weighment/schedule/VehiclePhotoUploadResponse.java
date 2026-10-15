package com.suguna.weighment_api_service.dto.weighment.schedule;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VehiclePhotoUploadResponse {

    private boolean success;
    private String scheduleId;
    /** First uploaded file (single-upload clients). */
    private VehiclePhotoInfo photo;
    /** All uploaded files (single or multiple). */
    private List<VehiclePhotoInfo> photos;

    @Getter
    @Builder
    public static class VehiclePhotoInfo {
        private String fileKey;
        private String uploadedAt;
    }
}
