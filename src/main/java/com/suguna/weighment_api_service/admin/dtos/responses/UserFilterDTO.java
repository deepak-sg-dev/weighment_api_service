package com.suguna.weighment_api_service.admin.dtos.responses;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFilterDTO {

    private String userType;   // CUSTOMER / EMPLOYEE
    private Integer role;       // ADMIN / USER
    private String status;     // ACTIVE / INACTIVE

    private int page = 0;
    private int size = 10;

}
