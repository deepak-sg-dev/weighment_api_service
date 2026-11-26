package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oracle.erp")
public class OracleErpProperties {

    private String applicationCode = "BRO-WEIGHMENT";

    /**
     * Stored on {@code SUG_MAI_WEIGHMENT_IMAGE_REF.APPLICATION} (may differ from lifting {@link #applicationCode}).
     */
    private String weighmentImageRefApplicationCode = "WMT_APP";

    /**
     * {@code sug_mai_devicedata.application} for mobile device login only. Lifting orders stay on
     * {@link #applicationCode} ({@code BRO-WEIGHMENT}).
     */
    private String deviceLoginApplicationCode = "WMT_APP";

    /** Written on successful device login ({@code sug_mai_devicedata.login_source}). */
    private String deviceLoginSource = "BRO-WEIGHMENT";
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
     * When true, retailer id/name/address (and GPS) come from Oracle HZ ship-to site on the OE order
     * ({@code oe_order_headers_all.ship_to_org_id} → {@code hz_cust_site_uses_all}).
     */
    private boolean retailCustomerFromHzSiteEnabled = true;

    /** {@code hz_cust_site_uses_all.site_use_code} (typically {@code SHIP_TO}). */
    private String retailCustomerSiteUseCode = "SHIP_TO";

    /** When true (and start column set), delivery weighment submit requires a prior start. */
    private boolean retailOrderDeliveryRequireStart = true;

    /**
     * {@code SUG_RETAIL_TABLES}: lifting → {@code SUG_MAI_RETAIL_ORDER_HDR} → DTL only (no OE/PO/HZ).
     * {@code OE_PO_CHAIN}: legacy link via {@code oe_order_lines_all.attribute2} and PO requisition.
     */
    private String retailOrderLinkMode = "OE_PO_CHAIN";

    /** When false, PO link matches {@code c.orig_sys_document_ref = b.segment1} (ERP reference SQL). */
    private boolean retailPoRequisitionJoinTrim = false;

    /**
     * MTL {@code mtl_system_items_b.segment1} for CULL lifting (DC HSN/tax). Example ERP item {@code BRCULL}.
     */
    private String dcItemCodeCull = "BRCULL";

    /**
     * MTL {@code segment1} for BROILER lifting. Empty skips item-master lookup until ERP item is confirmed.
     */
    private String dcItemCodeBroiler;

    /**
     * Fallback {@code sug_fnd_pkg.get_reg_off_address(p_ledger_id)} when the lifting branch has no
     * {@code sug_organization_mv.ledger_id}. M47 / branch 689 uses {@code 2025}.
     */
    private Long dcRegisteredOfficeLedgerId = 2025L;

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public String getWeighmentImageRefApplicationCode() {
        return weighmentImageRefApplicationCode;
    }

    public void setWeighmentImageRefApplicationCode(String weighmentImageRefApplicationCode) {
        this.weighmentImageRefApplicationCode = weighmentImageRefApplicationCode;
    }

    public String getDeviceLoginApplicationCode() {
        return deviceLoginApplicationCode;
    }

    public void setDeviceLoginApplicationCode(String deviceLoginApplicationCode) {
        this.deviceLoginApplicationCode = deviceLoginApplicationCode;
    }

    public String getDeviceLoginSource() {
        return deviceLoginSource;
    }

    public void setDeviceLoginSource(String deviceLoginSource) {
        this.deviceLoginSource = deviceLoginSource;
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

    public boolean isRetailOrderDeliveryRequireStart() {
        return retailOrderDeliveryRequireStart;
    }

    public void setRetailOrderDeliveryRequireStart(boolean retailOrderDeliveryRequireStart) {
        this.retailOrderDeliveryRequireStart = retailOrderDeliveryRequireStart;
    }

    public String getRetailOrderLinkMode() {
        return retailOrderLinkMode;
    }

    public void setRetailOrderLinkMode(String retailOrderLinkMode) {
        this.retailOrderLinkMode = retailOrderLinkMode;
    }

    public boolean isRetailOrderSugTablesLink() {
        return retailOrderLinkMode == null
                || retailOrderLinkMode.isBlank()
                || "SUG_RETAIL_TABLES".equalsIgnoreCase(retailOrderLinkMode.trim());
    }

    public boolean isRetailPoRequisitionJoinTrim() {
        return retailPoRequisitionJoinTrim;
    }

    public void setRetailPoRequisitionJoinTrim(boolean retailPoRequisitionJoinTrim) {
        this.retailPoRequisitionJoinTrim = retailPoRequisitionJoinTrim;
    }

    public String getDcItemCodeCull() {
        return dcItemCodeCull;
    }

    public void setDcItemCodeCull(String dcItemCodeCull) {
        this.dcItemCodeCull = dcItemCodeCull;
    }

    public String getDcItemCodeBroiler() {
        return dcItemCodeBroiler;
    }

    public void setDcItemCodeBroiler(String dcItemCodeBroiler) {
        this.dcItemCodeBroiler = dcItemCodeBroiler;
    }

    public Long getDcRegisteredOfficeLedgerId() {
        return dcRegisteredOfficeLedgerId;
    }

    public void setDcRegisteredOfficeLedgerId(Long dcRegisteredOfficeLedgerId) {
        this.dcRegisteredOfficeLedgerId = dcRegisteredOfficeLedgerId;
    }
}
