package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScheduleListResponseDTO {

    private List<ScheduleItemDTO> items;

    private PaginationDTO pagination;
}