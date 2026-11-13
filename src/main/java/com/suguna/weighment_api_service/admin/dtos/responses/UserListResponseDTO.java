package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserListResponseDTO {

    private UserSummaryDTO summary;

    private List<UserListItemDTO> items;

    private PaginationDTO pagination;
}
