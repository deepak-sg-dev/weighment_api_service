package com.suguna.weighment_api_service.admin.mappers;



import com.suguna.weighment_api_service.admin.dtos.requests.UserRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserResponseDTO;
import com.suguna.weighment_api_service.admin.entities.AppUser;
import com.suguna.weighment_api_service.admin.entities.Role;
import com.suguna.weighment_api_service.admin.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapper {

    @Autowired
    RoleRepository repository;

    public AppUser toEntity(UserRequestDTO dto) {
        AppUser user = new AppUser();
        user.setUserName(dto.getUserName());
        user.setPassword(dto.getPassword()); // encrypt later
        user.setName(dto.getName());
        user.setMobile(dto.getMobile());
        user.setEmail(dto.getEmail());
        user.setUserType(dto.getUserType());
        user.setApplication(dto.getApplication());
        user.setRoleId(dto.getRoleId());
        user.setOrgId(dto.getBranchId());
        return user;
    }

    public UserResponseDTO toDTO(AppUser user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setName(user.getName());
        dto.setMobile(user.getMobile());
        dto.setEmail(user.getEmail());
        dto.setOrgId(user.getOrgId());
        dto.setUserType(user.getUserType());
        dto.setApplication(user.getApplication());
        dto.setStatus(user.getStatus().equals("A")?"ACTIVE":"INACTIVE");
        /*Optional<Role> role = repository.findById(user.getRoleId());
        dto.setRole(role.isPresent()?role.get().getRoleName():"");*/


        // ✅ MAP ROLE NAME
        if (user.getRole() != null) {
            dto.setRole(user.getRole().getRoleName());
        }


        return dto;
    }
}