package com.suguna.weighment_api_service.admin.mappers;



import com.suguna.weighment_api_service.admin.dtos.requests.RoleRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleResponseDTO;
import com.suguna.weighment_api_service.admin.entities.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public Role toEntity(RoleRequestDTO dto) {
        Role role = new Role();
        role.setApplication(dto.getApplication());
        role.setRoleName(dto.getRoleName());
        role.setRoleDesc(dto.getRoleDesc());
        role.setRoleDesc(dto.getRoleFor());
        return role;
    }

    public RoleResponseDTO toDTO(Role role) {
        RoleResponseDTO dto = new RoleResponseDTO();
        dto.setId(role.getId());
        dto.setApplication(role.getApplication());
        dto.setRoleName(role.getRoleName());
        dto.setRoleDesc(role.getRoleDesc());
        dto.setStatus(role.getStatus());
        dto.setFeedmillRequired(role.getFeedmillRequired());
        dto.setFranchiseeRequired(role.getFranchiseeRequired());
        dto.setRoleFor(role.getRoleFor());
        return dto;
    }
}


