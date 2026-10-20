package com.suguna.weighment_api_service.storage;

/** Verification image type within a schedule folder. */
public enum WeighmentImageKind {
    VEHICLE("vehicle"),
    COMPLETION("completion"),
    SHOP("shop"),
    SIGNATURE("signature"),
    CUSTOMER("customer"),
    /** General / miscellaneous images for a schedule or order. */
    OTHER("other");

    private final String fileStem;

    WeighmentImageKind(String fileStem) {
        this.fileStem = fileStem;
    }

    public String fileStem() {
        return fileStem;
    }
}
