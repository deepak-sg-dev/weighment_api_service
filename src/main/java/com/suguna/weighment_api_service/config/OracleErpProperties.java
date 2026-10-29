package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oracle.erp")
public class OracleErpProperties {

    private String applicationCode = "BRO-WEIGHMENT";
    private int defaultGeofenceRadiusMeters = 200;
    private int standardBirdsPerCage = 12;
    private long stableDurationMs = 2000L;
    /** Limit list API to recent orders (0 = no date filter). */
    private int scheduleListLookbackDays = 90;

    /** {@code sug_mai_birds_lifting_order.source} value for retail lifting schedules. */
    private String retailLiftingSource = "RETAIL";

    /**
     * When true, delivery complete / delivery weighment submit validate shop OTP when configured on the line.
     */
    private boolean retailOtpValidationEnabled = true;

    /**
     * When true, read shop OTP from {@link #retailOrderOtpColumn} on {@code sug_mai_retail_order_dtl}
     * (enable after DDL). When false, only legacy {@code RETAIL_OTP:} in {@code message} is used.
     */
    private boolean retailOrderOtpUseDtlColumn = false;

    private String retailOrderOtpColumn = "RETAIL_OTP";

    private String retailOrderOtpExpiresColumn = "OTP_EXPIRES_AT";

    private String retailOrderOtpVerifiedColumn = "OTP_VERIFIED";

    /**
     * When true, retailer id/name/address (and optional GPS) come from Oracle HZ ship-to site on the OE order
     * ({@code oe_order_headers_all.ship_to_org_id} → {@code hz_cust_site_uses_all}).
     */
    private boolean retailCustomerFromHzSiteEnabled = true;

    /** {@code hz_cust_site_uses_all.site_use_code} (typically {@code SHIP_TO}). */
    private String retailCustomerSiteUseCode = "SHIP_TO";

    /**
     * When true, shop coordinates are read from {@code hz_cust_acct_sites_all} flex attributes when
     * {@link #retailCustomerFromHzSiteEnabled} is on; otherwise from {@code sug_mai_retail_order_dtl}.
     */
    private boolean retailCustomerGpsEnabled = false;

    /** Site flex attribute for latitude when {@link #retailCustomerFromHzSiteEnabled}; else DTL column name. */
    private String retailCustomerLatitudeColumn = "ATTRIBUTE11";

    /** Site flex attribute for longitude when {@link #retailCustomerFromHzSiteEnabled}; else DTL column name. */
    private String retailCustomerLongitudeColumn = "ATTRIBUTE10";

    /**
     * When true, {@code POST /weighment/deliveries/{orderId}/start} persists
     * {@link #retailOrderDeliveryStartedDateColumn} on {@code sug_mai_retail_order_dtl}.
     */
    private boolean retailOrderDeliveryStartEnabled = false;

    private String retailOrderDeliveryStartedDateColumn = "DELV_START_DT";

    private String retailOrderDeliveryCompletedDateColumn = "DELV_END_DT";

    /**
     * When true, {@code DELV_STATUS} (see {@link #retailOrderDeliveryStatusColumn}) is read/written on
     * {@code sug_mai_retail_order_dtl}; schedule {@code delivery_status} aggregates these values.
     */
    private boolean retailOrderDeliveryStatusEnabled = false;

    private String retailOrderDeliveryStatusColumn = "DELV_STATUS";

    /** When true (and start column enabled), delivery weighment submit requires a prior start. */
    private boolean retailOrderDeliveryRequireStart = true;

    /** Schema for OE / PO tables in the retail order link chain. */
    private String oracleAppsSchema = "APPS";

    /** Schema for {@code SUG_MAI_RETAIL_ORDER_*} and lifting tables. */
    private String oracleSugSchema = "SUG";

    private String retailOrderHdrTable = "SUG_MAI_RETAIL_ORDER_HDR";

    private String retailOrderDtlTable = "SUG_MAI_RETAIL_ORDER_DTL";

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public int getDefaultGeofenceRadiusMeters() {
        return defaultGeofenceRadiusMeters;
    }

    public void setDefaultGeofenceRadiusMeters(int defaultGeofenceRadiusMeters) {
        this.defaultGeofenceRadiusMeters = defaultGeofenceRadiusMeters;
    }

    public int getStandardBirdsPerCage() {
        return standardBirdsPerCage;
    }

    public void setStandardBirdsPerCage(int standardBirdsPerCage) {
        this.standardBirdsPerCage = standardBirdsPerCage;
    }

    public long getStableDurationMs() {
        return stableDurationMs;
    }

    public void setStableDurationMs(long stableDurationMs) {
        this.stableDurationMs = stableDurationMs;
    }

    public int getScheduleListLookbackDays() {
        return scheduleListLookbackDays;
    }

    public void setScheduleListLookbackDays(int scheduleListLookbackDays) {
        this.scheduleListLookbackDays = scheduleListLookbackDays;
    }

    public String getRetailLiftingSource() {
        return retailLiftingSource;
    }

    public void setRetailLiftingSource(String retailLiftingSource) {
        this.retailLiftingSource = retailLiftingSource;
    }

    public boolean isRetailOtpValidationEnabled() {
        return retailOtpValidationEnabled;
    }

    public void setRetailOtpValidationEnabled(boolean retailOtpValidationEnabled) {
        this.retailOtpValidationEnabled = retailOtpValidationEnabled;
    }

    public boolean isRetailOrderOtpUseDtlColumn() {
        return retailOrderOtpUseDtlColumn;
    }

    public void setRetailOrderOtpUseDtlColumn(boolean retailOrderOtpUseDtlColumn) {
        this.retailOrderOtpUseDtlColumn = retailOrderOtpUseDtlColumn;
    }

    public String getRetailOrderOtpColumn() {
        return retailOrderOtpColumn;
    }

    public void setRetailOrderOtpColumn(String retailOrderOtpColumn) {
        this.retailOrderOtpColumn = retailOrderOtpColumn;
    }

    public String getRetailOrderOtpExpiresColumn() {
        return retailOrderOtpExpiresColumn;
    }

    public void setRetailOrderOtpExpiresColumn(String retailOrderOtpExpiresColumn) {
        this.retailOrderOtpExpiresColumn = retailOrderOtpExpiresColumn;
    }

    public String getRetailOrderOtpVerifiedColumn() {
        return retailOrderOtpVerifiedColumn;
    }

    public void setRetailOrderOtpVerifiedColumn(String retailOrderOtpVerifiedColumn) {
        this.retailOrderOtpVerifiedColumn = retailOrderOtpVerifiedColumn;
    }

    public boolean isRetailCustomerFromHzSiteEnabled() {
        return retailCustomerFromHzSiteEnabled;
    }

    public void setRetailCustomerFromHzSiteEnabled(boolean retailCustomerFromHzSiteEnabled) {
        this.retailCustomerFromHzSiteEnabled = retailCustomerFromHzSiteEnabled;
    }

    public String getRetailCustomerSiteUseCode() {
        return retailCustomerSiteUseCode;
    }

    public void setRetailCustomerSiteUseCode(String retailCustomerSiteUseCode) {
        this.retailCustomerSiteUseCode = retailCustomerSiteUseCode;
    }

    public boolean isRetailCustomerGpsEnabled() {
        return retailCustomerGpsEnabled;
    }

    public void setRetailCustomerGpsEnabled(boolean retailCustomerGpsEnabled) {
        this.retailCustomerGpsEnabled = retailCustomerGpsEnabled;
    }

    public String getRetailCustomerLatitudeColumn() {
        return retailCustomerLatitudeColumn;
    }

    public void setRetailCustomerLatitudeColumn(String retailCustomerLatitudeColumn) {
        this.retailCustomerLatitudeColumn = retailCustomerLatitudeColumn;
    }

    public String getRetailCustomerLongitudeColumn() {
        return retailCustomerLongitudeColumn;
    }

    public void setRetailCustomerLongitudeColumn(String retailCustomerLongitudeColumn) {
        this.retailCustomerLongitudeColumn = retailCustomerLongitudeColumn;
    }

    public boolean isRetailOrderDeliveryStartEnabled() {
        return retailOrderDeliveryStartEnabled;
    }

    public void setRetailOrderDeliveryStartEnabled(boolean retailOrderDeliveryStartEnabled) {
        this.retailOrderDeliveryStartEnabled = retailOrderDeliveryStartEnabled;
    }

    public String getRetailOrderDeliveryStartedDateColumn() {
        return retailOrderDeliveryStartedDateColumn;
    }

    public void setRetailOrderDeliveryStartedDateColumn(String retailOrderDeliveryStartedDateColumn) {
        this.retailOrderDeliveryStartedDateColumn = retailOrderDeliveryStartedDateColumn;
    }

    public String getRetailOrderDeliveryCompletedDateColumn() {
        return retailOrderDeliveryCompletedDateColumn;
    }

    public void setRetailOrderDeliveryCompletedDateColumn(String retailOrderDeliveryCompletedDateColumn) {
        this.retailOrderDeliveryCompletedDateColumn = retailOrderDeliveryCompletedDateColumn;
    }

    public boolean isRetailOrderDeliveryStatusEnabled() {
        return retailOrderDeliveryStatusEnabled;
    }

    public void setRetailOrderDeliveryStatusEnabled(boolean retailOrderDeliveryStatusEnabled) {
        this.retailOrderDeliveryStatusEnabled = retailOrderDeliveryStatusEnabled;
    }

    public String getRetailOrderDeliveryStatusColumn() {
        return retailOrderDeliveryStatusColumn;
    }

    public void setRetailOrderDeliveryStatusColumn(String retailOrderDeliveryStatusColumn) {
        this.retailOrderDeliveryStatusColumn = retailOrderDeliveryStatusColumn;
    }

    public boolean isRetailOrderDeliveryRequireStart() {
        return retailOrderDeliveryRequireStart;
    }

    public void setRetailOrderDeliveryRequireStart(boolean retailOrderDeliveryRequireStart) {
        this.retailOrderDeliveryRequireStart = retailOrderDeliveryRequireStart;
    }

    public String getOracleAppsSchema() {
        return oracleAppsSchema;
    }

    public void setOracleAppsSchema(String oracleAppsSchema) {
        this.oracleAppsSchema = oracleAppsSchema;
    }

    public String getOracleSugSchema() {
        return oracleSugSchema;
    }

    public void setOracleSugSchema(String oracleSugSchema) {
        this.oracleSugSchema = oracleSugSchema;
    }

    public String getRetailOrderHdrTable() {
        return retailOrderHdrTable;
    }

    public void setRetailOrderHdrTable(String retailOrderHdrTable) {
        this.retailOrderHdrTable = retailOrderHdrTable;
    }

    public String getRetailOrderDtlTable() {
        return retailOrderDtlTable;
    }

    public void setRetailOrderDtlTable(String retailOrderDtlTable) {
        this.retailOrderDtlTable = retailOrderDtlTable;
    }
}
