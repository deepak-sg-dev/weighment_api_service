// RetailOrderHdrEntity_1.java
package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "SUG_MAI_RETAIL_ORDER_HDR")
@Getter
@NoArgsConstructor
public class RetailOrderHdrEntity_1 {

    @Id
    @Column(name = "HEADER_ID")
    private Long headerId;

    @Column(name = "REQ_HEADER_ID")
    private Long reqHeaderId;
}
