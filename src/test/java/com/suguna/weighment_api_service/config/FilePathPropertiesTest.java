package com.suguna.weighment_api_service.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FilePathPropertiesTest {

    @Test
    void resolve_prefersWeighmentVerificationDir() {
        FilePathProperties props = new FilePathProperties();
        props.setWeighmentVerificationDir("/data/weighment");
        props.setUploadDir("/data/uploads");
        assertEquals("/data/weighment", props.resolveWeighmentVerificationRoot(null));
    }

    @Test
    void resolve_usesUploadDirThenLegacy() {
        FilePathProperties props = new FilePathProperties();
        props.setUploadDir("/data/uploads");
        assertEquals("/data/uploads", props.resolveWeighmentVerificationRoot("/legacy"));
        props.setUploadDir(null);
        props.setUploadBasePath("/data/base");
        assertEquals("/data/base", props.resolveWeighmentVerificationRoot("/legacy"));
        props.setUploadBasePath(null);
        assertEquals("/legacy", props.resolveWeighmentVerificationRoot("/legacy"));
        assertEquals("uploads", props.resolveWeighmentVerificationRoot(null));
    }
}
