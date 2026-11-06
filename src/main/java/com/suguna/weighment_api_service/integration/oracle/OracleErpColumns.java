package com.suguna.weighment_api_service.integration.oracle;

/** Fixed Oracle ERP column and table names (see {@code document/*.sql}). */
public final class OracleErpColumns {

    public static final String SCHEMA_APPS = "APPS";
    public static final String SCHEMA_SUG = "SUG";

    public static final String RETAIL_ORDER_HDR = "SUG_MAI_RETAIL_ORDER_HDR";
    public static final String RETAIL_ORDER_DTL = "SUG_MAI_RETAIL_ORDER_DTL";
    public static final String RETAIL_HDR_LIFTING_MATCH = "REQ_HEADER_ID";

    public static final String RETAIL_OTP = "RETAIL_OTP";
    public static final String OTP_EXPIRES_AT = "OTP_EXPIRES_AT";
    public static final String OTP_VERIFIED = "OTP_VERIFIED";

    public static final String DELV_STATUS = "DELV_STATUS";
    public static final String DELV_START_DT = "DELV_START_DT";
    public static final String DELV_END_DT = "DELV_END_DT";

    public static final String DELV_VEHICLE_NO = "DELV_VEHICLE_NO";
    public static final String DELV_DRIVER = "DELV_DRIVER";
    public static final String DELV_DRIVER_MOB = "DELV_DRIVER_MOB";

    public static final String WEIGHMENT_IMAGE_REF = "SUG_MAI_WEIGHMENT_IMAGE_REF";

    public static final String LIVE_READING_AVG = "LIVE_READING_AVG";

    public static final String HZ_SITE_LATITUDE = "ATTRIBUTE11";
    public static final String HZ_SITE_LONGITUDE = "ATTRIBUTE10";
    public static final String HZ_LOCATION_ALIAS = "hl_ord";
    public static final String[] HZ_ADDRESS_PARTS = {"ADDRESS1", "ADDRESS2", "CITY"};

    private OracleErpColumns() {}
}
