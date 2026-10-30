package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "SUG_MAI_LIFTING_FARM_STOCK")
@Getter
@Setter
public class Farmer {

    @Id
    @Column(name = "FARMERCODE")
    private String farmerCode;

    @Column(name = "FARMERNAME")
    private String farmerName;

    @Column(name = "BRANCHCODE")
    private String branchCode;
}