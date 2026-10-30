package com.suguna.weighment_api_service.admin.dtos.requests;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FirebaseTokenDTO {

    private Long userId;
    private String firebaseToken;
}