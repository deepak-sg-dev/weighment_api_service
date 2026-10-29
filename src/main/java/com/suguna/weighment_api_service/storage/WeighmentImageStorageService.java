package com.suguna.weighment_api_service.storage;

import com.suguna.weighment_api_service.config.FilePathProperties;
import com.suguna.weighment_api_service.config.WeighmentUploadProperties;
import com.suguna.weighment_api_service.constants.ErrorCode;
import com.suguna.weighment_api_service.exception.ApiException;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores verification images under:
 *
 * <pre>
 * {baseDir}/lifting/{retail|wholesale}/{scheduleNo}/{scheduleId}-{yyMMddHHmmssSSS}.jpg
 * {baseDir}/delivery/.../{orderId}-{yyMMddHHmmssSSS}.jpg
 * Multiple files in one request share the same datetime token with -001, -002 suffixes.
 * </pre>
 */
@Service
public class WeighmentImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/jpg", "image/png");

    private final FilePathProperties filePathProperties;
    private final WeighmentUploadProperties uploadProperties;

    public WeighmentImageStorageService(
            FilePathProperties filePathProperties, WeighmentUploadProperties uploadProperties) {
        this.filePathProperties = filePathProperties;
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
        return storeScheduleImage(
                photo, domain, saleType, scheduleId, kind, optionalRefId, newCompactDateTimeToken(), 1, 1);
    }

    public java.util.List<StoredWeighmentImage> storeScheduleImages(
            java.util.List<MultipartFile> photos,
            WeighmentImageDomain domain,
            String saleType,
            String scheduleId,
            WeighmentImageKind kind,
            String optionalRefId) {
        int total = photos.size();
        String compactDateTime = newCompactDateTimeToken();
        java.util.List<StoredWeighmentImage> stored = new java.util.ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            stored.add(storeScheduleImage(
                    photos.get(i),
                    domain,
                    saleType,
                    scheduleId,
                    kind,
                    optionalRefId,
                    compactDateTime,
                    i + 1,
                    total));
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
            String compactDateTime,
            int sequenceOneBased,
            int totalInBatch) {
        validatePhoto(photo);

        String normalizedSchedule = ScheduleIdSupport.normalize(scheduleId);
        String scheduleFolder = sanitizePathSegment(normalizedSchedule);
        String saleFolder = saleTypeFolder(saleType);
        String fileName = buildFileName(
                kind,
                normalizedSchedule,
                optionalRefId,
                extensionFor(photo),
                compactDateTime,
                sequenceOneBased,
                totalInBatch);

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
        String root = filePathProperties.resolveWeighmentVerificationRoot(uploadProperties.getBaseDir());
        return Path.of(root).toAbsolutePath().normalize();
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

    private static final DateTimeFormatter COMPACT_UTC =
            DateTimeFormatter.ofPattern("yyMMddHHmmssSSS").withZone(ZoneOffset.UTC);

    /**
     * Short names: {@code {typeCode}-{scheduleId|orderId}-{yyMMddHHmmssSSS}}. Folder path holds domain/sale/schedule.
     */
    static String buildFileName(
            WeighmentImageKind kind,
            String scheduleId,
            String orderId,
            String extension,
            String compactDateTime,
            int sequenceOneBased,
            int totalInBatch) {
        String batchSeq = totalInBatch > 1 ? "-" + String.format("%03d", sequenceOneBased) : "";
        String idPart = fileNamePrefix(kind, scheduleId, orderId);
        return kind.fileCode() + "-" + idPart + "-" + compactDateTime + batchSeq + extension;
    }

    static String fileNamePrefix(WeighmentImageKind kind, String scheduleId, String orderId) {
        if (usesOrderIdInFileName(kind)) {
            if (orderId == null || orderId.isBlank()) {
                return sanitizePathSegment(scheduleId);
            }
            return sanitizePathSegment(orderId);
        }
        return sanitizePathSegment(scheduleId);
    }

    static boolean usesOrderIdInFileName(WeighmentImageKind kind) {
        return kind == WeighmentImageKind.SHOP
                || kind == WeighmentImageKind.SIGNATURE
                || kind == WeighmentImageKind.CUSTOMER;
    }

    /** UTC {@code yyMMddHHmmssSSS} (17 digits), e.g. {@code 251026143045123}. */
    static String newCompactDateTimeToken() {
        return COMPACT_UTC.format(Instant.now());
    }

    private void validatePhoto(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "photo is required");
        }
        String contentType = resolveImageContentType(photo);
        if (contentType == null) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "photo must be JPEG or PNG");
        }
        if (photo.getSize() > uploadProperties.getMaxImageBytes()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "photo exceeds maximum allowed size");
        }
    }

    /**
     * Mobile clients (e.g. Flutter) often send {@code application/octet-stream} or omit Content-Type;
     * infer from filename when needed.
     */
    static String resolveImageContentType(MultipartFile photo) {
        String raw = photo.getContentType();
        if (raw != null && !raw.isBlank()) {
            String contentType = raw.trim();
            if (ALLOWED_CONTENT_TYPES.stream().anyMatch(t -> t.equalsIgnoreCase(contentType))) {
                return contentType.toLowerCase(Locale.ROOT).contains("png") ? "image/png" : "image/jpeg";
            }
            if (contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                if (contentType.toLowerCase(Locale.ROOT).contains("png")) {
                    return "image/png";
                }
                if (contentType.toLowerCase(Locale.ROOT).contains("jpeg")
                        || contentType.toLowerCase(Locale.ROOT).contains("jpg")) {
                    return "image/jpeg";
                }
            }
        }
        String name = photo.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".png")) {
                return "image/png";
            }
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                return "image/jpeg";
            }
        }
        return null;
    }

    private static String extensionFor(MultipartFile photo) {
        String contentType = resolveImageContentType(photo);
        if (contentType != null && contentType.contains("png")) {
            return ".png";
        }
        return ".jpg";
    }

    public record StoredWeighmentImage(String relativeFileKey, Path absolutePath) {}
}
