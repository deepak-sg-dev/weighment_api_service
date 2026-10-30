package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "SUG_MAI_LIFTING_CUSTOMER_LOC")
@Getter
@Setter
public class TraderDepot {

    @Id
    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "SHIP_LOCATION")
    private String shipLocation;
}