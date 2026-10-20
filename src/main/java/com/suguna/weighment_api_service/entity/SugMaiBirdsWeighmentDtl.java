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
 * Maps {@code APPS.SUG_MAI_BIRDS_WEIGHMENT_DTL}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SUG_MAI_BIRDS_WEIGHMENT_DTL", schema = "APPS")
public class SugMaiBirdsWeighmentDtl {

    @Id
    @Column(name = "DTL_ID")
    private BigDecimal dtlId;

    @Column(name = "BUSINESS_MODULE", nullable = false)
    private String businessModule;

    @Column(name = "TXN_PHASE", nullable = false)
    private String txnPhase;

    @Column(name = "HEADER_REF_TYPE", nullable = false)
    private String headerRefType;

    @Column(name = "HEADER_REF_ID", nullable = false)
    private String headerRefId;

    @Column(name = "SCHEDULE_NO")
    private BigDecimal scheduleNo;

    @Column(name = "TRANS_ID")
    private BigDecimal transId;

    @Column(name = "RETAIL_ORDER_NO")
    private BigDecimal retailOrderNo;

    @Column(name = "RETAILER_CODE")
    private String retailerCode;

    @Column(name = "WM_SUPERVISOR_CODE")
    private String wmSupervisorCode;

    @Column(name = "LOCAL_TRANSACTION_ID", nullable = false)
    private String localTransactionId;

    @Column(name = "SUBMIT_BATCH_NO", nullable = false)
    private BigDecimal submitBatchNo;

    @Column(name = "LINE_SEQUENCE", nullable = false)
    private BigDecimal lineSequence;

    @Column(name = "CAGE_OR_SET_REF")
    private String cageOrSetRef;

    @Column(name = "BIRDS_APPLIED")
    private BigDecimal birdsApplied;

    @Column(name = "BIRDS_ADJUSTED")
    private String birdsAdjusted;

    @Column(name = "ADJUSTMENT_REASON")
    private String adjustmentReason;

    @Column(name = "TARE_WT")
    private BigDecimal tareWt;

    @Column(name = "GROSS_WT")
    private BigDecimal grossWt;

    @Column(name = "NET_WT")
    private BigDecimal netWt;

    @Column(name = "AVG_BIRD_WT")
    private BigDecimal avgBirdWt;

    @Column(name = "TOLERANCE_STATUS")
    private String toleranceStatus;

    @Column(name = "CAPTURED_AT")
    private LocalDateTime capturedAt;

    @Column(name = "EVENT_AT")
    private LocalDateTime eventAt;

    @Column(name = "CAPTURE_LAT")
    private BigDecimal captureLat;

    @Column(name = "CAPTURE_LONG")
    private BigDecimal captureLong;

    @Column(name = "CAPTURE_ACCURACY_M")
    private BigDecimal captureAccuracyM;

    @Column(name = "SCALE_NO")
    private String scaleNo;

    @Column(name = "DEVICE_ID")
    private String deviceId;

    @Column(name = "ROW_STATUS", nullable = false)
    private String rowStatus;

    @Column(name = "EXT_JSON")
    @Lob
    private String extJson;

    @Column(name = "STATUS", nullable = false)
    private Long status;

    @Column(name = "CREATED_BY")
    private BigDecimal createdBy;

    @Column(name = "CREATION_DATE", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "UPDATED_BY")
    private BigDecimal updatedBy;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

}