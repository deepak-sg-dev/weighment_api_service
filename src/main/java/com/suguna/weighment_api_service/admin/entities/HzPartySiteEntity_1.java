// HzPartySiteEntity_1.java
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
@Table(name = "HZ_PARTY_SITES")
@Getter
@NoArgsConstructor
public class HzPartySiteEntity_1 {

    @Id
    @Column(name = "PARTY_SITE_ID")
    private Long partySiteId;

    @Column(name = "LOCATION_ID")
    private Long locationId;

    @Column(name = "IDENTIFYING_ADDRESS_FLAG")
    private String identifyingAddressFlag;
}
