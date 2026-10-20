package com.suguna.weighment_api_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps {@code SUG.SUG_MAI_NTAIL_ORDER}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Getter
@Setter
@NoArgsConstructor
@IdClass(SugMaiNtailOrder.SugMaiNtailOrderId.class)
@Entity
@Table(name = "SUG_MAI_NTAIL_ORDER", schema = "SUG")
public class SugMaiNtailOrder {

    @Column(name = "REFID")
    private BigDecimal refid;

    @Column(name = "REGION")
    private String region;

    @Column(name = "CUSTOMER_CODE")
    private String customerCode;

    @Id
    @Column(name = "BRANCH_CODE")
    private String branchCode;

    @Column(name = "ITEM_NAME")
    private String itemName;

    @Column(name = "ITEM_TYPE")
    private String itemType;

    @Column(name = "QUANTITY")
    private BigDecimal quantity;

    @Column(name = "WEIGHT")
    private BigDecimal weight;

    @Id
    @Column(name = "TRANS_DATE")
    private LocalDateTime transDate;

    @Id
    @Column(name = "ORDER_NO")
    private BigDecimal orderNo;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "UPLOADED_FLAG")
    private String uploadedFlag;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "UPLOADED_DATE")
    private LocalDateTime uploadedDate;

    @Column(name = "NTAIL_ORDER_ID")
    private BigDecimal ntailOrderId;

    @Getter
    @Setter
    public static class SugMaiNtailOrderId implements java.io.Serializable {
        private BigDecimal orderNo;
        private String branchCode;
        private LocalDateTime transDate;
    }
}