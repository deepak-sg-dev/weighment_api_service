package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.dto.weighment.schedule.VehiclePhotoUploadResponse;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.storage.WeighmentImageCategory;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class WeighmentDeliveryPhotoService {

    private final WeighmentImageUploadService imageUploadService;

    public WeighmentDeliveryPhotoService(WeighmentImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    public VehiclePhotoUploadResponse uploadShopPhotos(
            AuthenticatedSupervisor supervisor,
            String scheduleId,
            String retailOrderId,
            List<MultipartFile> photos,
            Instant capturedAt) {
        return imageUploadService.toVehiclePhotoResponse(imageUploadService.upload(
                supervisor,
                scheduleId,
                WeighmentImageCategory.SHOP,
                retailOrderId,
                null,
                photos,
                capturedAt,
                true));
    }
}
