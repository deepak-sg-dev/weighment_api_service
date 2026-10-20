package com.suguna.weighment_api_service.dto.weighment.schedule;

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
public class VehiclePhotoUploadResponse {

    private boolean success;
    private String scheduleId;
    /** First uploaded file (single-upload clients). */
    private VehiclePhotoInfo photo;
    /** All uploaded files (single or multiple). */
    private List<VehiclePhotoInfo> photos;

@NoArgsConstructor
@AllArgsConstructor
@Builder
    @Getter
    @Setter
    public static class VehiclePhotoInfo {
        private String fileKey;
        private String uploadedAt;
    }
}