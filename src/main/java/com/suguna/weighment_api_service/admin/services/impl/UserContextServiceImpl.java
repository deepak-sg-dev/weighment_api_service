package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.BranchAccessDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.PermissionResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleDTO;
import com.suguna.weighment_api_service.admin.entities.AppUser;
import com.suguna.weighment_api_service.admin.entities.Menu;
import com.suguna.weighment_api_service.admin.entities.Role;
import com.suguna.weighment_api_service.admin.entities.RoleMenuAccess;
import com.suguna.weighment_api_service.admin.repositories.AppUserRepository;
import com.suguna.weighment_api_service.admin.repositories.MenuRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleMenuAccessRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleRepository;
import com.suguna.weighment_api_service.admin.services.UserContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserContextServiceImpl implements UserContextService {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleMenuAccessRepository roleMenuAccessRepository;

    @Override
    public PermissionResponseDTO getPermissions(Long userId) {

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        List<RoleMenuAccess> accesses =
                roleMenuAccessRepository.findByRoleIdAndStatus(
                        role.getId(),
                        "A");

        List<String> permissions = new ArrayList<>();

        for (RoleMenuAccess access : accesses) {

            Menu menu = menuRepository.findById(
                            access.getMenuId())
                    .orElse(null);

            if (menu == null) {
                continue;
            }

            String module = menu.getMenuName()
                    .trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]+", ".");

            module = module.replaceAll("^\\.|\\.$", "");

            if ("Y".equalsIgnoreCase(access.getCanRead())) {
                permissions.add(module + ".read");
            }

            if ("Y".equalsIgnoreCase(access.getCanWrite())) {
                permissions.add(module + ".write");
            }

            if ("Y".equalsIgnoreCase(access.getCanUpdate())) {
                permissions.add(module + ".update");
            }

            if ("Y".equalsIgnoreCase(access.getCanDelete())) {
                permissions.add(module + ".delete");
            }
        }

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setName(role.getRoleName());
        roleDTO.setDisplayName(role.getRoleName());

        PermissionResponseDTO response =
                new PermissionResponseDTO();

        response.setRole(roleDTO);

        response.setBranchAccess(
                Collections.<BranchAccessDTO>emptyList());

        response.setPermissions(permissions);

        response.setScope("ASSIGNED_BRANCHES");

        return response;
    }
}