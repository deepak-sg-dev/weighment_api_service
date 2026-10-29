package com.suguna.weighment_api_service.storage;

/** Verification image type within a schedule folder. */
public enum WeighmentImageKind {
    VEHICLE("vehicle", "vh"),
    COMPLETION("completion", "cp"),
    SHOP("shop", "sh"),
    SIGNATURE("signature", "sg"),
    CUSTOMER("customer", "cu"),
    /** General / miscellaneous images for a schedule or order. */
    OTHER("other", "ot");

    private final String fileStem;
    /** Two-letter file prefix (type + schedule/order id + datetime). */
    private final String fileCode;

    WeighmentImageKind(String fileStem, String fileCode) {
        this.fileStem = fileStem;
        this.fileCode = fileCode;
    }

    public String fileStem() {
        return fileStem;
    }

    public String fileCode() {
        return fileCode;
    }
}
