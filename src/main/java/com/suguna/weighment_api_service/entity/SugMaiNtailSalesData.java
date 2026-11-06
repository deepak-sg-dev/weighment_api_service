package com.suguna.weighment_api_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps {@code SUG.SUG_MAI_NTAIL_SALES_DATA}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SUG_MAI_NTAIL_SALES_DATA", schema = "SUG")
public class SugMaiNtailSalesData {

    @Id
    @Column(name = "ID")
    private BigDecimal id;

    @Column(name = "COMPANY_CODE")
    private BigDecimal companyCode;

    @Column(name = "ISV_SUPERVISOR_CODE")
    private String isvSupervisorCode;

    @Column(name = "ISV_RETAILER_CODE")
    private String isvRetailerCode;

    @Column(name = "DC_NO")
    private Long dcNo;

    @Column(name = "TOTAL_WEIGHT")
    private BigDecimal totalWeight;

    @Column(name = "NO_OF_BIRDS")
    private BigDecimal noOfBirds;

    @Column(name = "RATE")
    private BigDecimal rate;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "START_DATE")
    private String startDate;

    @Column(name = "START_TIME")
    private String startTime;

    @Column(name = "END_DATE")
    private String endDate;

    @Column(name = "END_TIME")
    private String endTime;

    @Column(name = "NO_OF_WEIGHMENTS")
    private BigDecimal noOfWeighments;

    @Column(name = "VEHICLE_NO")
    private String vehicleNo;

    @Column(name = "SCALE_NO")
    private String scaleNo;

    @Column(name = "MORT_WT")
    private BigDecimal mortWt;

    @Column(name = "REPL_WT")
    private BigDecimal replWt;

    @Column(name = "ISV_DEALER_CODE")
    private String isvDealerCode;

    @Column(name = "BIRD_TYPE")
    private String birdType;

    @Column(name = "TRAY_ISSUED")
    private BigDecimal trayIssued;

    @Column(name = "TRAY_RECEIVED")
    private BigDecimal trayReceived;

    @Column(name = "ISV_VEHICLE_ID")
    private String isvVehicleId;

    @Column(name = "ISV_SUPPLIER_CODE")
    private String isvSupplierCode;

    @Column(name = "PURCHASE_ID")
    private BigDecimal purchaseId;

    @Column(name = "ENTRY_TYPE")
    private String entryType;

    @Column(name = "WORKING_MODE")
    private String workingMode;

    @Column(name = "LOCAL_DC_NO")
    private Long localDcNo;

    @Column(name = "LOCAL_RECEIPT_NO")
    private Long localReceiptNo;

    @Column(name = "ITEM_NAME")
    private String itemName;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "ORDERDBID")
    private BigDecimal orderdbid;

}