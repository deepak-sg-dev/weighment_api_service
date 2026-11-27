// RetailOrderDtlEntity_1.java
package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "SUG_MAI_RETAIL_ORDER_DTL")
@Getter
@NoArgsConstructor
public class RetailOrderDtlEntity_1 {

    @Id
    @Column(name = "LINE_ID")
    private Long lineId;

    @Column(name = "HEADER_ID")
    private Long headerId;

    @Column(name = "CUSTOMER_CODE")
    private String customerCode;

    @Column(name = "WEIGHT")
    private BigDecimal weight;

    @Column(name = "QUANTITY")
    private BigDecimal quantity;

    @Column(name = "DELV_STATUS")
    private String delvStatus;

    @Column(name = "DELV_END_DT")
    private LocalDateTime delvEndDt;
}
