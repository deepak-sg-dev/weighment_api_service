package com.suguna.weighment_api_service.admin.services;



import com.suguna.weighment_api_service.admin.dtos.responses.UserFilterDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UserRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.PageResponse;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserRequestDTO dto);

    UserResponseDTO updateUser(Long id, UserRequestDTO dto);

    UserResponseDTO getUserById(Long id);

    List<UserResponseDTO> getAllUsers();

    String deleteUser(Long id);

    PageResponse<UserResponseDTO> getUsers(UserFilterDTO filter);

    void updateFirebaseToken(Long userId, String token);
}

