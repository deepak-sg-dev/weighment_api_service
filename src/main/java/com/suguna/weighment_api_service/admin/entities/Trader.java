package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "SUG_MAI_LIFTING_CUSTOMER_MST")
@Getter
@Setter
public class Trader {

    @Id
    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "CUSTOMER_NUMBER")
    private String customerNumber;

    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @Column(name = "BRANCH_CODE")
    private String branchCode;
}
