package com.suguna.weighment_api_service.admin.services.impl;



import com.suguna.weighment_api_service.admin.dtos.requests.MenuRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuTreeDTO;
import com.suguna.weighment_api_service.admin.entities.PosMenu;
import com.suguna.weighment_api_service.admin.entities.RoleMenuAccess;
import com.suguna.weighment_api_service.admin.mappers.MenuMapper;
import com.suguna.weighment_api_service.admin.repositories.MenuRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleMenuAccessRepository;
import com.suguna.weighment_api_service.admin.services.MenuService;
import com.suguna.weighment_api_service.admin.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuRepository repository;

    @Autowired
    private MenuMapper mapper;

    @Autowired
    private RoleMenuAccessRepository roleMenuAccessRepository;

    @Autowired
    private UserUtils userUtils;

    @Override
    public MenuResponseDTO createMenu(MenuRequestDTO dto) {
        PosMenu menu = mapper.toEntity(dto);
        menu.setCreatedAt(new Date());
        menu.setStatus("A");

        return mapper.toDTO(repository.save(menu));
    }

    @Override
    public MenuResponseDTO updateMenu(Long id, MenuRequestDTO dto) {
        PosMenu menu = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        menu.setMenuName(dto.getMenuName());
        menu.setMenuPath(dto.getMenuPath());
        menu.setParentId(dto.getParentId());
        menu.setApplication(dto.getApplication());
        menu.setUpdatedAt(new Date());

        return mapper.toDTO(repository.save(menu));
    }

    @Override
    public MenuResponseDTO getMenuById(Long id) {
        return mapper.toDTO(
                repository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Menu not found"))
        );
    }

    @Override
    public List<MenuResponseDTO> getAllMenus() {
        return repository.findByIsDeleted(0)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }


    @Override
    public List<MenuTreeDTO> getMenuTree(String application) {

        List<PosMenu> menus = repository.findByApplicationAndStatusAndIsDeletedOrderBySequenceAsc(application, "A",0);

        // Convert all menus to map (id → DTO)
        Map<Long, MenuTreeDTO> map = new HashMap<>();

        for (PosMenu menu : menus) {
            MenuTreeDTO dto = new MenuTreeDTO();
            dto.setId(menu.getId());
            dto.setMenuName(menu.getMenuName());
            dto.setMenuPath(menu.getMenuPath());
            dto.setMenuType(menu.getMenuType());
            map.put(menu.getId(), dto);
        }

        List<MenuTreeDTO> rootMenus = new ArrayList<>();

        for (PosMenu menu : menus) {

            MenuTreeDTO current = map.get(menu.getId());

            if (menu.getParentId() == null || menu.getParentId() == 0) {
                // Root menu
                rootMenus.add(current);
            } else {
                // Child menu
                MenuTreeDTO parent = map.get(menu.getParentId());

                if (parent != null) {
                    parent.getChildren().add(current);
                }
            }
        }

        return rootMenus;
    }

    @Override
    public List<MenuResponseDTO> getMenusByRole(Long roleId) {

        // ✅ Step 1: Get all menus
        List<PosMenu> menus = repository.findByIsDeleted(0);

        // ✅ Step 2: Get role assigned menus
        List<RoleMenuAccess> roleMenus =
                roleMenuAccessRepository.findByRoleIdAndStatus(roleId, "A");

        // ✅ Step 3: Convert to Map (menuId → access)
        Map<Long, RoleMenuAccess> roleMap = roleMenus.stream()
                .collect(Collectors.toMap(
                        RoleMenuAccess::getMenuId,
                        r -> r,
                        (r1, r2) -> r1
                ));

        // ✅ Step 4: Map menus with permissions
        return menus.stream()
                .map(menu -> {

                    MenuResponseDTO dto = mapper.toDTO(menu);

                    RoleMenuAccess access = roleMap.get(menu.getId());

                    if (access != null) {

                        // ✅ assigned
                        dto.setAssigned(true);

                        // ✅ permissions
                        dto.setCanRead("Y".equalsIgnoreCase(access.getCanRead()));
                        dto.setCanWrite("Y".equalsIgnoreCase(access.getCanWrite()));
                        dto.setCanUpdate("Y".equalsIgnoreCase(access.getCanUpdate()));
                        dto.setCanDelete("Y".equalsIgnoreCase(access.getCanDelete()));

                    } else {

                        // ❌ not assigned
                        dto.setAssigned(false);

                        dto.setCanRead(false);
                        dto.setCanWrite(false);
                        dto.setCanUpdate(false);
                        dto.setCanDelete(false);
                    }

                    return dto;
                })
                .toList();
    }

    @Override
    public void deleteMenu(Long id) {

        PosMenu menu = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        // ✅ Prevent double delete
        if (menu.getIsDeleted() == 1) {
            throw new RuntimeException("Menu already deleted");
        }

        // ✅ Optional: Prevent deleting parent menu with children
        List<PosMenu> children = repository
                .findByParentIdAndIsDeleted(menu.getId(),0);

        if (children != null && !children.isEmpty()) {
            throw new RuntimeException(
                    "Cannot delete menu. It has child menus."
            );
        }

        // ✅ Soft Delete
        menu.setIsDeleted(1);
        menu.setDeletedAt(new Date());
        Long userId = userUtils.getCurrentUserId();;
        menu.setDeletedBy(userId);

        // ✅ Optional (if you track user)
        // menu.setDeletedBy(currentUserId);

        repository.save(menu);
    }

    @Override
    public void restoreMenu(Long id) {

        PosMenu menu = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        menu.setIsDeleted(0);
        menu.setDeletedAt(null);

        repository.save(menu);
    }


}

