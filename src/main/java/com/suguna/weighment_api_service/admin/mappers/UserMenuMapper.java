package com.suguna.weighment_api_service.admin.mappers;



import com.suguna.weighment_api_service.admin.dtos.requests.UserMenuAccessRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserMenuAccessResponseDTO;
import com.suguna.weighment_api_service.admin.entities.UserMenuAccess;
import org.springframework.stereotype.Component;

@Component
public class UserMenuMapper {

    public UserMenuAccess toEntity(UserMenuAccessRequestDTO dto, Long userId) {
        UserMenuAccess entity = new UserMenuAccess();
        entity.setUserId(userId);
        entity.setMenuId(dto.getMenuId());
        entity.setCanRead(dto.getCanRead());
        entity.setCanWrite(dto.getCanWrite());
        entity.setCanUpdate(dto.getCanUpdate());
        entity.setCanDelete(dto.getCanDelete());
        return entity;
    }

    public UserMenuAccessResponseDTO toDTO(UserMenuAccess entity) {
        UserMenuAccessResponseDTO dto = new UserMenuAccessResponseDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setMenuId(entity.getMenuId());
        dto.setCanRead(entity.getCanRead());
        dto.setCanWrite(entity.getCanWrite());
        dto.setCanUpdate(entity.getCanUpdate());
        dto.setCanDelete(entity.getCanDelete());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}

