package com.suguna.weighment_api_service.storage;

/** Top-level folder under the weighment upload root. */
public enum WeighmentImageDomain {
    LIFTING("lifting"),
    DELIVERY("delivery");

    private final String folderName;

    WeighmentImageDomain(String folderName) {
        this.folderName = folderName;
    }

    public String folderName() {
        return folderName;
    }
}
