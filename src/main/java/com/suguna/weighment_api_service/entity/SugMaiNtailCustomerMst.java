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
 * Maps {@code SUG.SUG_MAI_NTAIL_CUSTOMER_MST}.
 * Read-only mapping ({@code ddl-auto=none}); data access remains JDBC-based.
 */
@Getter
@Setter
@NoArgsConstructor
@IdClass(SugMaiNtailCustomerMst.SugMaiNtailCustomerMstId.class)
@Entity
@Table(name = "SUG_MAI_NTAIL_CUSTOMER_MST", schema = "SUG")
public class SugMaiNtailCustomerMst {

    @Id
    @Column(name = "CUSTOMER_ID")
    private BigDecimal customerId;

    @Id
    @Column(name = "CUSTOMER_NUMBER")
    private String customerNumber;

    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @Column(name = "REGION")
    private String region;

    @Column(name = "MOBILE_NO")
    private String mobileNo;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Column(name = "UPLOADED_FLAG")
    private String uploadedFlag;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "UPLOADED_DATE")
    private LocalDateTime uploadedDate;

    @Column(name = "BRANCH_CODE")
    private String branchCode;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SugMaiNtailCustomerMstId implements java.io.Serializable {
        private BigDecimal customerId;
        private String customerNumber;
    }
}
