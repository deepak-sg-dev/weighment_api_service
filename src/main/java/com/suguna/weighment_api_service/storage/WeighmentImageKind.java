package com.suguna.weighment_api_service.storage;

/** Verification image type within a schedule folder. */
public enum WeighmentImageKind {
    VEHICLE("vehicle", "vh"),
    COMPLETION("completion", "cp"),
    /** Lifting: farmer signature (file name uses schedule id). */
    FARMER_SIGNATURE("farmer-signature", "ff"),
    /** Lifting: supervisor signature (schedule id). */
    LIFTING_SUPERVISOR_SIGNATURE("lifting-supervisor-signature", "ls"),
    /** Lifting: trader signature — wholesale (schedule id). */
    TRADER_SIGNATURE("trader-signature", "tr"),
    SHOP("shop", "sh"),
    /** Delivery: customer / retailer signature (order id). */
    SIGNATURE("signature", "sg"),
    /** Delivery: supervisor signature at shop (order id). */
    DELIVERY_SUPERVISOR_SIGNATURE("delivery-supervisor-signature", "ds"),
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
