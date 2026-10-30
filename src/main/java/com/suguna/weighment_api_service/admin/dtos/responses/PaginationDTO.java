package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationDTO {

    private Integer page;
    private Integer limit;
    private Long total;
}

