package com.suguna.weighment_api_service.dto.auth;

import com.suguna.weighment_api_service.domain.SupervisorProfile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupervisorDto {

    private String id;
    private String code;
    private String name;
    private String mobile;
    private String branchId;
    private String branchName;

    public static SupervisorDto from(SupervisorProfile profile) {
        return SupervisorDto.builder()
                .id(profile.getId())
                .code(profile.getCode())
                .name(profile.getName())
                .mobile(profile.getMobile())
                .branchId(profile.getBranchId())
                .branchName(profile.getBranchName())
                .build();
    }
}
