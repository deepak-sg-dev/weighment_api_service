package com.suguna.weighment_api_service.constants;

/** Admin-configurable weighment flow steps (V2 get-schedule {@code workflow}). */
public enum WeighmentWorkflowStep {
    GEOFENCING,
    BLE_SCALE_CONNECTION,
    OTP_VERIFICATION,
    QR_VERIFICATION,
    VEHICLE_PHOTO,
    VEHICLE_TIMELINE,
    OTP_VALIDATION,
    QR_VALIDATION,
    SHOP_PHOTO,
    CUSTOMER_SIGNATURE_PHOTO,
    SHOP_STOCK
}
