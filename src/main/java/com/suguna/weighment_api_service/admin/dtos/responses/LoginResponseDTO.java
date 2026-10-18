package com.suguna.weighment_api_service.admin.dtos.responses;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {


    private String token;

    private Long userId;
    private String username;
    private String name;
    private Long roleId;     // since you use single role
    private Long orgId;
    private String userType;
    private String companyName;
    private String branchName;
    private String branchAddress;
    private String branchTownOrCity;
    private String postalCode;
    private String kraPin;
    private String mobileNumber;
    private Date lastLogin;
    private Boolean passwordExpired;

    private List<MenuTreeDTO> menus;  // IMPORTANT


}