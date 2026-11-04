package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SupervisorListResponseDTO {

    private List<SupervisorResponseDTO> items;

    private PaginationDTO pagination;
}