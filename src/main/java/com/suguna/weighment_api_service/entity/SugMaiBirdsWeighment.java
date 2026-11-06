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
 * Maps {@code SUG.SUG_MAI_BIRDS_WEIGHMENT}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SUG_MAI_BIRDS_WEIGHMENT", schema = "SUG")
public class SugMaiBirdsWeighment {

    @Id
    @Column(name = "TRANS_ID")
    private BigDecimal transId;

    @Column(name = "DC_DATE")
    private LocalDateTime dcDate;

    @Column(name = "BRANCH_CODE")
    private String branchCode;

    @Column(name = "BRANCH_NAME")
    private String branchName;

    @Column(name = "FARM_CODE")
    private String farmCode;

    @Column(name = "FARM_NAME")
    private String farmName;

    @Column(name = "BATCH_ID")
    private BigDecimal batchId;

    @Column(name = "CUSTOMER_CODE")
    private String customerCode;

    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @Column(name = "CUSTOMER_LOCATION")
    private String customerLocation;

    @Column(name = "VEHICLE_TYPE")
    private String vehicleType;

    @Column(name = "VEHICLE_NO")
    private String vehicleNo;

    @Column(name = "DIRVER_NAME")
    private String dirverName;

    @Column(name = "VEHICLE_INTIME")
    private LocalDateTime vehicleIntime;

    @Column(name = "VEHICLE_OUTTIME")
    private LocalDateTime vehicleOuttime;

    @Column(name = "WM_START_TIME")
    private LocalDateTime wmStartTime;

    @Column(name = "WM_END_TIME")
    private LocalDateTime wmEndTime;

    @Column(name = "WM_SUPERVISOR_CODE")
    private String wmSupervisorCode;

    @Column(name = "WM_SUPERVISOR_NAME")
    private String wmSupervisorName;

    @Column(name = "ITEM_ID")
    private BigDecimal itemId;

    @Column(name = "NO_OF_CAGES")
    private BigDecimal noOfCages;

    @Column(name = "EMPTY_WT")
    private BigDecimal emptyWt;

    @Column(name = "GROSS_WT")
    private BigDecimal grossWt;

    @Column(name = "NET_WT")
    private BigDecimal netWt;

    @Column(name = "NO_OF_BIRDS")
    private BigDecimal noOfBirds;

    @Column(name = "AVG_WT")
    private BigDecimal avgWt;

    @Column(name = "MANUAL_DC_NO")
    private String manualDcNo;

    @Column(name = "SCALE_NO")
    private String scaleNo;

    @Column(name = "BIRD_TYPE")
    private String birdType;

    @Column(name = "BIRD_STOCK_NOS")
    private BigDecimal birdStockNos;

    @Column(name = "FEED_STOCK_BAGS")
    private BigDecimal feedStockBags;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "ORDER_NUMBER")
    private String orderNumber;

    @Column(name = "ORDER_DATE")
    private LocalDateTime orderDate;

    @Column(name = "ORDERED_QTY")
    private BigDecimal orderedQty;

    @Column(name = "FARM_BIRD_STOCK")
    private BigDecimal farmBirdStock;

    @Column(name = "UPDATED_BY")
    private BigDecimal updatedBy;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ORDER_HEADER_ID")
    private BigDecimal orderHeaderId;

    @Column(name = "ORDER_LINE_ID")
    private BigDecimal orderLineId;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "VEHICLE_LOAD_TYPE")
    private String vehicleLoadType;

    @Column(name = "SELLING_PRICE")
    private BigDecimal sellingPrice;

    @Column(name = "SOURCE")
    private String source;

    @Column(name = "REF_ID")
    private BigDecimal refId;

    @Column(name = "P_FARM_STOCK")
    private BigDecimal pFarmStock;

    @Column(name = "SCHEDULE_NO")
    private BigDecimal scheduleNo;

    @Column(name = "EXCESS_QTY")
    private BigDecimal excessQty;

    @Column(name = "EXCESS_REMARKS")
    private String excessRemarks;

    @Column(name = "LATITUDE")
    private BigDecimal latitude;

    @Column(name = "LONGITUDE")
    private BigDecimal longitude;

    @Column(name = "FINAL_PRICE")
    private String finalPrice;

    @Column(name = "ERRORCOUNT")
    private BigDecimal errorcount;

    @Column(name = "ABORTCOUNT")
    private BigDecimal abortcount;

    @Column(name = "NPICK_STATUS")
    private String npickStatus;

}