package com.suguna.weighment_api_service.storage;

import com.suguna.weighment_api_service.config.WeighmentUploadProperties;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores verification images under:
 *
 * <pre>
 * {baseDir}/lifting/{retail|wholesale}/{scheduleNo}/vehicle.jpg
 * {baseDir}/delivery/{retail|wholesale}/{scheduleNo}/order-{id}-shop.jpg
 * </pre>
 */
@Service
public class WeighmentImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/jpg", "image/png");

    private final WeighmentUploadProperties uploadProperties;

    public WeighmentImageStorageService(WeighmentUploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public StoredWeighmentImage storeScheduleImage(
            MultipartFile photo,
            WeighmentImageDomain domain,
            String saleType,
            String scheduleId,
            WeighmentImageKind kind) {
        return storeScheduleImage(photo, domain, saleType, scheduleId, kind, null);
    }

    public StoredWeighmentImage storeScheduleImage(
            MultipartFile photo,
            WeighmentImageDomain domain,
            String saleType,
            String scheduleId,
            WeighmentImageKind kind,
            String optionalRefId) {
        return storeScheduleImage(photo, domain, saleType, scheduleId, kind, optionalRefId, 1, 1);
    }

    public java.util.List<StoredWeighmentImage> storeScheduleImages(
            java.util.List<MultipartFile> photos,
            WeighmentImageDomain domain,
            String saleType,
            String scheduleId,
            WeighmentImageKind kind,
            String optionalRefId) {
        int total = photos.size();
        java.util.List<StoredWeighmentImage> stored = new java.util.ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            stored.add(storeScheduleImage(
                    photos.get(i), domain, saleType, scheduleId, kind, optionalRefId, i + 1, total));
        }
        return stored;
    }

    public StoredWeighmentImage storeScheduleImage(
            MultipartFile photo,
            WeighmentImageDomain domain,
            String saleType,
            String scheduleId,
            WeighmentImageKind kind,
            String optionalRefId,
            int sequenceOneBased,
            int totalInBatch) {
        validatePhoto(photo);

        String normalizedSchedule = ScheduleIdSupport.normalize(scheduleId);
        String scheduleFolder = sanitizePathSegment(normalizedSchedule);
        String saleFolder = saleTypeFolder(saleType);
        String fileName = buildFileName(
                kind, optionalRefId, extensionFor(photo.getContentType()), sequenceOneBased, totalInBatch);

        Path directory = resolveBaseDir()
                .resolve(domain.folderName())
                .resolve(saleFolder)
                .resolve(scheduleFolder);

        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(fileName);
            photo.transferTo(target);
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Failed to store image");
        }

        String relativeKey = domain.folderName() + "/" + saleFolder + "/" + scheduleFolder + "/" + fileName;
        return new StoredWeighmentImage(relativeKey, directory.resolve(fileName));
    }

    public Path resolveBaseDir() {
        return Path.of(uploadProperties.getBaseDir()).toAbsolutePath().normalize();
    }

    public static String saleTypeFolder(String saleType) {
        if (saleType == null || saleType.isBlank()) {
            return "wholesale";
        }
        String normalized = saleType.trim().toUpperCase(Locale.ROOT);
        if ("RETAIL".equals(normalized)) {
            return "retail";
        }
        return "wholesale";
    }

    static String sanitizePathSegment(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    static String buildFileName(
            WeighmentImageKind kind,
            String optionalRefId,
            String extension,
            int sequenceOneBased,
            int totalInBatch) {
        String suffix = totalInBatch > 1 ? "-" + String.format("%03d", sequenceOneBased) : "";
        if (kind == WeighmentImageKind.SHOP && optionalRefId != null && !optionalRefId.isBlank()) {
            return "order-" + sanitizePathSegment(optionalRefId) + "-" + kind.fileStem() + suffix + extension;
        }
        if (optionalRefId != null && !optionalRefId.isBlank()) {
            return kind.fileStem() + "-" + sanitizePathSegment(optionalRefId) + suffix + extension;
        }
        return kind.fileStem() + suffix + extension;
    }

    private void validatePhoto(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "photo is required");
        }
        String contentType = photo.getContentType();
        if (contentType == null
                || ALLOWED_CONTENT_TYPES.stream().noneMatch(t -> t.equalsIgnoreCase(contentType.trim()))) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "photo must be JPEG or PNG");
        }
        if (photo.getSize() > uploadProperties.getMaxImageBytes()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "photo exceeds maximum allowed size");
        }
    }

    private static String extensionFor(String contentType) {
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains("png")) {
            return ".png";
        }
        return ".jpg";
    }

    public record StoredWeighmentImage(String relativeFileKey, Path absolutePath) {}
}
