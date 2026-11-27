// OeOrderHeaderEntity_1.java
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
@Table(name = "OE_ORDER_HEADERS_ALL")
@Getter
@NoArgsConstructor
public class OeOrderHeaderEntity_1 {

    @Id
    @Column(name = "HEADER_ID")
    private Long headerId;

    @Column(name = "SOURCE_DOCUMENT_ID")
    private Long sourceDocumentId;
}
