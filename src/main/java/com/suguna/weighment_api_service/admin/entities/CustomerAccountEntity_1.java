// CustomerAccountEntity_1.java
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
@Table(name = "HZ_CUST_ACCOUNTS")
@Getter
@NoArgsConstructor
public class CustomerAccountEntity_1 {

    @Id
    @Column(name = "CUST_ACCOUNT_ID")
    private Long custAccountId;

    @Column(name = "ACCOUNT_NUMBER")
    private String accountNumber;

    @Column(name = "PARTY_ID")
    private Long partyId;
}
