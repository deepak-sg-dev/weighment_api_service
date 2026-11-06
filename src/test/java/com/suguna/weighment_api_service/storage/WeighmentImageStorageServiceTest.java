package com.suguna.weighment_api_service.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class WeighmentImageStorageServiceTest {

    @Test
    void resolveImageContentType_acceptsOctetStreamWithJpegExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("application/octet-stream");
        when(file.getOriginalFilename()).thenReturn("capture.jpg");
        assertEquals("image/jpeg", WeighmentImageStorageService.resolveImageContentType(file));
    }

    @Test
    void resolveImageContentType_acceptsStandardMime() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("shop.png");
        assertEquals("image/png", WeighmentImageStorageService.resolveImageContentType(file));
    }

    @Test
    void resolveImageContentType_rejectsUnknown() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("doc.pdf");
        assertNull(WeighmentImageStorageService.resolveImageContentType(file));
    }

    @Test
    void buildFileName_shortScheduleOrOrderPlusDateTime() {
        String vehicle = WeighmentImageStorageService.buildFileName(
                WeighmentImageKind.VEHICLE, "4747402", null, ".png", "251026143045123", 1, 1);
        assertEquals("vh-4747402-251026143045123.png", vehicle);

        String firstOfBatch = WeighmentImageStorageService.buildFileName(
                WeighmentImageKind.VEHICLE, "4747402", null, ".jpg", "251026143045123", 1, 3);
        assertEquals("vh-4747402-251026143045123-001.jpg", firstOfBatch);

        String shop = WeighmentImageStorageService.buildFileName(
                WeighmentImageKind.SHOP, "4747402", "ORD99", ".png", "251026143045123", 1, 1);
        assertEquals("sh-ORD99-251026143045123.png", shop);

        String signature = WeighmentImageStorageService.buildFileName(
                WeighmentImageKind.SIGNATURE, "4747402", "ORD99", ".png", "251026143100456", 1, 1);
        assertEquals("sg-ORD99-251026143100456.png", signature);
        assertTrue(WeighmentImageStorageService.usesOrderIdInFileName(WeighmentImageKind.SHOP));
    }
}
