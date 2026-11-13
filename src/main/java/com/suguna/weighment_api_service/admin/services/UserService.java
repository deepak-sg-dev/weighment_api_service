package com.suguna.weighment_api_service.admin.services;

import com.suguna.weighment_api_service.admin.dtos.responses.UserListItemDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserListResponseDTO;

public interface UserService {

    UserListResponseDTO getUsers(
            Integer page,
            Integer limit,
            String search,
            String userType,
            Long roleId,
            String status);

    UserListItemDTO getUserById(Long id);
}
