// OrganizationMvEntity_1.java
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
@Table(name = "SUG_ORGANIZATION_MV")
@Getter
@NoArgsConstructor
public class OrganizationMvEntity_1 {

    @Id
    @Column(name = "BRANCH_CODE")
    private String branchCode;

    @Column(name = "BRANCH_NAME")
    private String branchName;
}
