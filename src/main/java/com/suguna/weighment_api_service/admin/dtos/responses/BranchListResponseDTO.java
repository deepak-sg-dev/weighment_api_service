package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BranchListResponseDTO {

    private List<BranchItemDTO> items;
    private PaginationDTO pagination;
}