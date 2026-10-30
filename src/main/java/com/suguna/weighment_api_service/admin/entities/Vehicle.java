package com.suguna.weighment_api_service.admin.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "SUG_TRUCK_NUMBERS_MV")
@Getter
@Setter
public class Vehicle {

    @Id
    @Column(name = "VEHICLE_NO")
    private String vehicleNo;

    @Column(name = "OPERATING_UNIT")
    private BigDecimal operatingUnit;
}