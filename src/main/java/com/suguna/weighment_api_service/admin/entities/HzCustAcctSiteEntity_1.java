// HzCustAcctSiteEntity_1.java
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
@Table(name = "HZ_CUST_ACCT_SITES_ALL")
@Getter
@NoArgsConstructor
public class HzCustAcctSiteEntity_1 {

    @Id
    @Column(name = "CUST_ACCT_SITE_ID")
    private Long custAcctSiteId;

    @Column(name = "CUST_ACCOUNT_ID")
    private Long custAccountId;

    @Column(name = "PARTY_SITE_ID")
    private Long partySiteId;

    @Column(name = "STATUS")
    private String status;
}
