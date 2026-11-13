package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegionItemDTO {

    private Long id;

    private String regionCode;

    private String name;

    private String zoneName;
}
