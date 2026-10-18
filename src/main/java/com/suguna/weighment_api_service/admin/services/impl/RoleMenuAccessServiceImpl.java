package com.suguna.weighment_api_service.admin.services.impl;


import com.suguna.weighment_api_service.admin.dtos.requests.RoleMenuAccessRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.RoleMenuBulkRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleMenuAccessResponseDTO;
import com.suguna.weighment_api_service.admin.entities.RoleMenuAccess;
import com.suguna.weighment_api_service.admin.mappers.RoleMenuMapper;
import com.suguna.weighment_api_service.admin.repositories.RoleMenuAccessRepository;
import com.suguna.weighment_api_service.admin.services.RoleMenuAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleMenuAccessServiceImpl implements RoleMenuAccessService {

    @Autowired
    private RoleMenuAccessRepository repository;

    @Autowired
    private RoleMenuMapper mapper;

    @Override
    public String assignMenusToRole(RoleMenuBulkRequestDTO dto) {


        Long roleId = dto.getRoleId();

        // ✅ Step 1: Fetch existing menus
        List<RoleMenuAccess> existingMenus =
                repository.findByRoleId(roleId);

        Map<Long, RoleMenuAccess> existingMap = existingMenus.stream()
                .collect(Collectors.toMap(
                        RoleMenuAccess::getMenuId,
                        e -> e,
                        (e1, e2) -> e1
                ));

        Set<Long> incomingMenuIds = dto.getMenus().stream()
                .map(RoleMenuAccessRequestDTO::getMenuId)
                .collect(Collectors.toSet());

        // ✅ Step 2: UPDATE or INSERT
        for (RoleMenuAccessRequestDTO menuDTO : dto.getMenus()) {

            RoleMenuAccess entity = existingMap.get(menuDTO.getMenuId());

            if (entity != null) {

                // ✅ UPDATE existing
                entity.setCanRead(menuDTO.getCanRead());
                entity.setCanWrite(menuDTO.getCanWrite());
                entity.setCanUpdate(menuDTO.getCanUpdate());
                entity.setCanDelete(menuDTO.getCanDelete());
                entity.setUpdatedAt(new Date());
                entity.setStatus("A");

                repository.save(entity);

            } else {

                // ✅ INSERT new
                RoleMenuAccess newEntity = mapper.toEntity(menuDTO);
                newEntity.setRoleId(roleId);
                newEntity.setCreatedAt(new Date());
                newEntity.setStatus("A");

                repository.save(newEntity);
            }
        }

        // ✅ Step 3: DELETE removed menus
        for (RoleMenuAccess existing : existingMenus) {

            if (!incomingMenuIds.contains(existing.getMenuId())) {

                // ✅ HARD DELETE (or use soft delete)
                repository.delete(existing);

                // ✅ OR soft delete:
                // existing.setStatus("I");
                // repository.save(existing);
            }
        }

        return "Menus updated successfully";

    }

    @Override
    public List<RoleMenuAccessResponseDTO> getMenusByRole(Long roleId) {

        return repository.findByRoleIdAndStatus(roleId, "A")
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public void updateRoleMenuAccess(RoleMenuBulkRequestDTO dto) {

        // Remove old mappings (soft delete)
        List<RoleMenuAccess> existing =
                repository.findByRoleIdAndStatus(dto.getRoleId(), "A");

        for (RoleMenuAccess e : existing) {
            e.setStatus("I");
            repository.save(e);
        }

        // Insert new mappings
        assignMenusToRole(dto);
    }

    @Override
    public void deleteRoleMenu(Long id) {

        RoleMenuAccess entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping not found"));

        entity.setStatus("I");
        repository.save(entity);
    }
}


