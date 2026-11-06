package com.suguna.weighment_api_service.admin.mappers;



import com.suguna.weighment_api_service.admin.dtos.requests.RoleMenuAccessRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleMenuAccessResponseDTO;
import com.suguna.weighment_api_service.admin.entities.RoleMenuAccess;
import org.springframework.stereotype.Component;

@Component
public class RoleMenuMapper {

    public RoleMenuAccess toEntity(RoleMenuAccessRequestDTO dto) {
        RoleMenuAccess entity = new RoleMenuAccess();
        entity.setRoleId(dto.getRoleId());
        entity.setMenuId(dto.getMenuId());
        entity.setCanRead(dto.getCanRead());
        entity.setCanWrite(dto.getCanWrite());
        entity.setCanUpdate(dto.getCanUpdate());
        entity.setCanDelete(dto.getCanDelete());
        return entity;
    }

    public RoleMenuAccessResponseDTO toDTO(RoleMenuAccess entity) {
        RoleMenuAccessResponseDTO dto = new RoleMenuAccessResponseDTO();
        dto.setId(entity.getId());
        dto.setRoleId(entity.getRoleId());
        dto.setMenuId(entity.getMenuId());
        dto.setCanRead(entity.getCanRead());
        dto.setCanWrite(entity.getCanWrite());
        dto.setCanUpdate(entity.getCanUpdate());
        dto.setCanDelete(entity.getCanDelete());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
