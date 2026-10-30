package com.suguna.weighment_api_service.admin.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceId implements Serializable {

    private BigDecimal deviceId;

    private String empCode;

    private String application;
}