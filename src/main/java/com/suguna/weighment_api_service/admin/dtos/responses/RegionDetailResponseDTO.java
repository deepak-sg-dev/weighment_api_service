package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegionDetailResponseDTO {

    private Long id;

    private String regionCode;

    private String region;

    private String zoneName;
}
