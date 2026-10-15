package com.suguna.weighment_api_service.storage;

/** Verification image type within a schedule folder. */
public enum WeighmentImageKind {
    VEHICLE("vehicle"),
    COMPLETION("completion"),
    SHOP("shop"),
    SIGNATURE("signature");

    private final String fileStem;

    WeighmentImageKind(String fileStem) {
        this.fileStem = fileStem;
    }

    public String fileStem() {
        return fileStem;
    }
}
