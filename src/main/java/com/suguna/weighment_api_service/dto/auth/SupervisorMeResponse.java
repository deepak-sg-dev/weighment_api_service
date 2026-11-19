package com.suguna.weighment_api_service.dto.auth;


import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SupervisorMeResponse {

    private boolean success;
    private String userType;
    private String role;
    private String vehicleNo;
    private java.util.List<LoginMenuDto> menus;
    private SupervisorDto supervisor;
    private DriverDto driver;
}