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
 * Maps {@code SUG.SUG_MAI_BIRDS_LIFTING_ORDER}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SUG_MAI_BIRDS_LIFTING_ORDER", schema = "SUG")
public class SugMaiBirdsLiftingOrder {

    @Id
    @Column(name = "ERPINDENTID")
    private String erpindentid;

    @Column(name = "BRANCHCODE")
    private String branchcode;

    @Column(name = "BRANCHID")
    private BigDecimal branchid;

    @Column(name = "CUSTOMERCODE", nullable = false)
    private String customercode;

    @Column(name = "CUS_NAME")
    private String cusName;

    @Column(name = "FARMERCODE")
    private String farmercode;

    @Column(name = "DRIVER")
    private String driver;

    @Column(name = "VEHICLENO")
    private String vehicleno;

    @Column(name = "VEHICLETYPE")
    private String vehicletype;

    @Column(name = "ORDERNUMBER", nullable = false)
    private BigDecimal ordernumber;

    @Column(name = "ORDER_REQUEST_DATE")
    private LocalDateTime orderRequestDate;

    @Column(name = "DI_CREATION_DATE")
    private LocalDateTime diCreationDate;

    @Column(name = "NO_OF_KGS")
    private BigDecimal noOfKgs;

    @Column(name = "NO_OF_BIRDS")
    private BigDecimal noOfBirds;

    @Column(name = "TOLERANCEPERCENT")
    private BigDecimal tolerancepercent;

    @Column(name = "MOBILEUSEREMPID")
    private String mobileuserempid;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "POSTED_FLAG")
    private String postedFlag;

    @Column(name = "POSTED_DATE")
    private LocalDateTime postedDate;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "REGIONCODE")
    private String regioncode;

    @Column(name = "APPLICATION")
    private String application;

    @Column(name = "CUS_LOCATION")
    private String cusLocation;

    @Column(name = "FARMERNAME")
    private String farmername;

    @Column(name = "FARMER_MOBILE_NO")
    private String farmerMobileNo;

    @Column(name = "TRADER_OTP")
    private String traderOtp;

    @Column(name = "MOBILE_ORDER_NO")
    private BigDecimal mobileOrderNo;

    @Column(name = "SOURCE")
    private String source;

    @Column(name = "FARMER_OTP")
    private String farmerOtp;

    @Column(name = "ORDERTYPE")
    private String ordertype;

    @Column(name = "BODY_WT")
    private BigDecimal bodyWt;

    @Column(name = "AVG_WT_LOW")
    private BigDecimal avgWtLow;

    @Column(name = "AVG_WT_HIGH")
    private BigDecimal avgWtHigh;

    @Column(name = "BREED")
    private String breed;

    @Column(name = "WEIGHMENT_TYPE")
    private String weighmentType;

    @Column(name = "DOWNLOAD_FLAG")
    private String downloadFlag;

    @Column(name = "DOWNLOADED_DATE")
    private LocalDateTime downloadedDate;

    @Column(name = "STARTED_DATE")
    private LocalDateTime startedDate;

    @Column(name = "LIFTING_STATUS")
    private String liftingStatus;

    @Column(name = "DELIVERY_STATUS")
    private String deliveryStatus;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

}