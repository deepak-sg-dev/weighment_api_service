package com.suguna.weighment_api_service.dto.weighment.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartyContactDto {

    private String id;
    private String name;
    private String mobile;
}
