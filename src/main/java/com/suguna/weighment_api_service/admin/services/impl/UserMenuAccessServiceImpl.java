package com.suguna.weighment_api_service.admin.services.impl;


import com.suguna.weighment_api_service.admin.dtos.requests.UserMenuAccessRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UserMenuBulkRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserMenuAccessResponseDTO;
import com.suguna.weighment_api_service.admin.entities.UserMenuAccess;
import com.suguna.weighment_api_service.admin.mappers.UserMenuMapper;
import com.suguna.weighment_api_service.admin.repositories.UserMenuAccessRepository;
import com.suguna.weighment_api_service.admin.services.UserMenuAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserMenuAccessServiceImpl implements UserMenuAccessService {

    @Autowired
    private UserMenuAccessRepository repository;

    @Autowired
    private UserMenuMapper mapper;

    @Override
    public String assignMenusToUser(UserMenuBulkRequestDTO dto) {

        for (UserMenuAccessRequestDTO menuDTO : dto.getMenus()) {

            UserMenuAccess entity = mapper.toEntity(menuDTO, dto.getUserId());
            entity.setCreatedAt(new Date());
            entity.setStatus("A");

            repository.save(entity);
        }
        return "Menu Assigned";
    }

    @Override
    public List<UserMenuAccessResponseDTO> getMenusByUser(Long userId) {

        return repository.findByUserIdAndStatus(userId, "A")
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public String updateUserMenuAccess(UserMenuBulkRequestDTO dto) {

        // Soft delete existing
        List<UserMenuAccess> existing =
                repository.findByUserIdAndStatus(dto.getUserId(), "A");

        for (UserMenuAccess e : existing) {
            e.setStatus("I");
            repository.save(e);
        }

        // Reinsert new
        assignMenusToUser(dto);

        return "Updated";
    }

    @Override
    public String removeUserMenuAccess(Long id) {

        UserMenuAccess entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User menu access not found"));

        entity.setStatus("I");
        repository.save(entity);
        return "Deleted Successfully";
    }
}


