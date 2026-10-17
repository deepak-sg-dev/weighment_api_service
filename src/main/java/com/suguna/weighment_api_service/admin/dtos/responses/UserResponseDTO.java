package com.suguna.weighment_api_service.admin.dtos.responses;




import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {

    private Long id;
    private String userName;
    private String name;
    private Long mobile;
    private String email;
    private Long orgId;
    private String userType;
    private String application;
    private String status;
    private String role;
}
