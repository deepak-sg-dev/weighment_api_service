// HzPartyEntity_1.java
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
@Table(name = "HZ_PARTIES")
@Getter
@NoArgsConstructor
public class HzPartyEntity_1 {

    @Id
    @Column(name = "PARTY_ID")
    private Long partyId;

    @Column(name = "PARTY_NAME")
    private String partyName;
}
