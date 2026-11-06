package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.requests.CreateRoleRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.RoleMenuDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateRolePermissionsRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.UpdateRoleRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.CreateRoleResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuItemDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleDetailResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleListItemDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RolePermissionDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UpdateResponseDTO;
import com.suguna.weighment_api_service.admin.entities.Menu;
import com.suguna.weighment_api_service.admin.entities.Role;
import com.suguna.weighment_api_service.admin.entities.RoleMenuAccess;
import com.suguna.weighment_api_service.admin.repositories.MenuRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleMenuAccessRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleRepository;
import com.suguna.weighment_api_service.admin.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMenuAccessRepository roleMenuAccessRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Override
    public CreateRoleResponseDTO createRole(
            CreateRoleRequestDTO request) {

        Role role = new Role();

        role.setApplication("WMT_APP");
        role.setRoleName(request.getRoleName());
        role.setRoleDesc(request.getRoleDescription());
        role.setRoleFor(request.getRoleFor());
        role.setStatus("A");

        role.setCreatedAt(new Date());
        role.setUpdatedAt(new Date());

        Role savedRole =
                roleRepository.save(role);

        if (request.getMenus() != null) {

            for (RoleMenuDTO menu : request.getMenus()) {

                RoleMenuAccess access =
                        new RoleMenuAccess();

                access.setRoleId(
                        savedRole.getId());

                access.setMenuId(
                        menu.getMenuId());

                access.setCanRead(
                        Boolean.TRUE.equals(menu.getCanRead())
                                ? "Y"
                                : "N");

                access.setCanWrite(
                        Boolean.TRUE.equals(menu.getCanWrite())
                                ? "Y"
                                : "N");

                access.setCanUpdate(
                        Boolean.TRUE.equals(menu.getCanUpdate())
                                ? "Y"
                                : "N");

                access.setCanDelete(
                        Boolean.TRUE.equals(menu.getCanDelete())
                                ? "Y"
                                : "N");

                access.setStatus("A");

                roleMenuAccessRepository.save(
                        access);
            }
        }

        CreateRoleResponseDTO response =
                new CreateRoleResponseDTO();

        response.setRoleId(
                savedRole.getId());

        return response;
    }

    @Override
    public RoleListResponseDTO getRoles() {

        List<RoleListItemDTO> items =
                roleRepository
                        .findByApplicationAndStatus(
                                "WMT_APP",
                                "A")
                        .stream()
                        .map(role -> {

                            RoleListItemDTO dto =
                                    new RoleListItemDTO();

                            dto.setId(role.getId());
                            dto.setRoleName(role.getRoleName());
                            dto.setRoleDescription(role.getRoleDesc());
                            dto.setRoleFor(role.getRoleFor());
                            dto.setStatus(role.getStatus());

                            return dto;
                        })
                        .toList();

        RoleListResponseDTO response =
                new RoleListResponseDTO();

        response.setItems(items);

        return response;
    }

    @Override
    public MenuListResponseDTO getMenus() {

        List<MenuItemDTO> items =
                menuRepository
                        .findByApplicationAndStatus(
                                "WMT_APP",
                                "A")
                        .stream()
                        .filter(menu ->
                                !"Menu".equalsIgnoreCase(
                                        menu.getMenuName())
                                        &&
                                        !"Retail".equalsIgnoreCase(
                                                menu.getMenuName())
                                        &&
                                        !"Wholesale".equalsIgnoreCase(
                                                menu.getMenuName()))
                        .map(menu -> {

                            MenuItemDTO dto =
                                    new MenuItemDTO();

                            dto.setId(menu.getId());
                            dto.setName(menu.getMenuName());
                            dto.setPath(menu.getMenuPath());

                            return dto;
                        })
                        .toList();

        MenuListResponseDTO response =
                new MenuListResponseDTO();

        response.setItems(items);

        return response;
    }

    @Override
    public RoleDetailResponseDTO getRole(
            Long roleId) {

        Role role =
                roleRepository.findById(roleId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Role not found"));

        List<RoleMenuAccess> accesses =
                roleMenuAccessRepository.findByRoleId(
                        roleId);

        List<RolePermissionDTO> menus =
                accesses.stream()
                        .map(access -> {

                            Menu menu =
                                    menuRepository
                                            .findById(
                                                    access.getMenuId())
                                            .orElse(null);

                            if (menu == null) {
                                return null;
                            }

                            RolePermissionDTO dto =
                                    new RolePermissionDTO();

                            dto.setMenuId(
                                    menu.getId());

                            dto.setMenuName(
                                    menu.getMenuName());

                            dto.setCanRead(
                                    "Y".equalsIgnoreCase(
                                            access.getCanRead()));

                            dto.setCanWrite(
                                    "Y".equalsIgnoreCase(
                                            access.getCanWrite()));

                            dto.setCanUpdate(
                                    "Y".equalsIgnoreCase(
                                            access.getCanUpdate()));

                            dto.setCanDelete(
                                    "Y".equalsIgnoreCase(
                                            access.getCanDelete()));

                            return dto;
                        })
                        .filter(Objects::nonNull)
                        .toList();

        RoleDetailResponseDTO response =
                new RoleDetailResponseDTO();

        response.setId(role.getId());
        response.setRoleName(role.getRoleName());
        response.setRoleDescription(role.getRoleDesc());
        response.setRoleFor(role.getRoleFor());
        response.setStatus(role.getStatus());
        response.setMenus(menus);

        return response;
    }

    @Override
    public UpdateResponseDTO updateRole(
            Long roleId,
            UpdateRoleRequestDTO request) {

        Role role =
                roleRepository.findById(roleId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Role not found"));

        if (request.getRoleName() != null) {
            role.setRoleName(
                    request.getRoleName());
        }

        if (request.getRoleDescription() != null) {
            role.setRoleDesc(
                    request.getRoleDescription());
        }

        if (request.getRoleFor() != null) {
            role.setRoleFor(
                    request.getRoleFor());
        }

        if (request.getStatus() != null) {
            role.setStatus(
                    request.getStatus());
        }

        role.setUpdatedAt(
                new Date());

        roleRepository.save(role);

        UpdateResponseDTO response =
                new UpdateResponseDTO();

        response.setUpdated(true);

        return response;
    }

    @Override
    public UpdateResponseDTO updatePermissions(
            Long roleId,
            UpdateRolePermissionsRequestDTO request) {

        roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found"));

        List<RoleMenuAccess> existing =
                roleMenuAccessRepository.findByRoleId(
                        roleId);

        roleMenuAccessRepository.deleteAll(
                existing);

        if (request.getMenus() != null) {

            for (RoleMenuDTO menu : request.getMenus()) {

                RoleMenuAccess access =
                        new RoleMenuAccess();

                access.setRoleId(
                        roleId);

                access.setMenuId(
                        menu.getMenuId());

                access.setCanRead(
                        Boolean.TRUE.equals(menu.getCanRead())
                                ? "Y"
                                : "N");

                access.setCanWrite(
                        Boolean.TRUE.equals(menu.getCanWrite())
                                ? "Y"
                                : "N");

                access.setCanUpdate(
                        Boolean.TRUE.equals(menu.getCanUpdate())
                                ? "Y"
                                : "N");

                access.setCanDelete(
                        Boolean.TRUE.equals(menu.getCanDelete())
                                ? "Y"
                                : "N");

                access.setStatus("A");

                roleMenuAccessRepository.save(
                        access);
            }
        }

        UpdateResponseDTO response =
                new UpdateResponseDTO();

        response.setUpdated(true);

        return response;
    }
}