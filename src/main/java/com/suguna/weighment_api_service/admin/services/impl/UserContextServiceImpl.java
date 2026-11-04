package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.BranchAccessDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.PermissionResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.RoleDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserContextResponseDTO;
import com.suguna.weighment_api_service.admin.entities.AppUser;
import com.suguna.weighment_api_service.admin.entities.Menu;
import com.suguna.weighment_api_service.admin.entities.OrganizationMV;
import com.suguna.weighment_api_service.admin.entities.Role;
import com.suguna.weighment_api_service.admin.entities.RoleMenuAccess;
import com.suguna.weighment_api_service.admin.entities.UserBranchMap;
import com.suguna.weighment_api_service.admin.repositories.AppUserRepository;
import com.suguna.weighment_api_service.admin.repositories.MenuRepository;
import com.suguna.weighment_api_service.admin.repositories.OrganizationMVRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleMenuAccessRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleRepository;
import com.suguna.weighment_api_service.admin.repositories.UserBranchMapRepository;
import com.suguna.weighment_api_service.admin.services.UserContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired
    private UserBranchMapRepository userBranchMapRepository;

    @Autowired
    private OrganizationMVRepository organizationMVRepository;

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
                        role.getId(), "A");

        List<String> permissions = new ArrayList<>();

        for (RoleMenuAccess access : accesses) {

            Menu menu = menuRepository.findById(access.getMenuId())
                    .orElse(null);

            if (menu == null) {
                continue;
            }

            String module = menu.getMenuName()
                    .trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]+", ".");

            module = module.replaceAll("^\\.|\\.$", "");

            if ("retail".equals(module)
                    || "wholesale".equals(module)) {
                continue;
            }

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

        List<UserBranchMap> mappings =
                userBranchMapRepository.findByUserIdAndStatus(
                        user.getId(),
                        "A");

        List<BranchAccessDTO> branchAccess =
                new ArrayList<>();

        for (UserBranchMap mapping : mappings) {

            OrganizationMV org =
                    organizationMVRepository
                            .findByBranchId(mapping.getBranchId())
                            .orElse(null);

            if (org == null) {
                continue;
            }

            BranchAccessDTO dto = new BranchAccessDTO();
            dto.setId(mapping.getBranchId());
            dto.setName(org.getBranchName());

            branchAccess.add(dto);
        }

        PermissionResponseDTO response =
                new PermissionResponseDTO();

        response.setRole(roleDTO);
        response.setBranchAccess(branchAccess);
        response.setPermissions(permissions);
        response.setScope("ASSIGNED_BRANCHES");

        return response;
    }

    @Override
    public UserContextResponseDTO getUserContext(Long userId) {

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        List<RoleMenuAccess> accesses =
                roleMenuAccessRepository.findByRoleIdAndStatus(
                        role.getId(), "A");

        List<String> permissions = new ArrayList<>();

        for (RoleMenuAccess access : accesses) {

            Menu menu = menuRepository.findById(access.getMenuId())
                    .orElse(null);

            if (menu == null) {
                continue;
            }

            String module = menu.getMenuName()
                    .trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]+", ".");

            module = module.replaceAll("^\\.|\\.$", "");

            if ("retail".equals(module)
                    || "wholesale".equals(module)) {
                continue;
            }

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

        List<UserBranchMap> mappings =
                userBranchMapRepository.findByUserIdAndStatus(
                        user.getId(),
                        "A");

        List<BranchAccessDTO> branchAccess =
                new ArrayList<>();

        for (UserBranchMap mapping : mappings) {

            OrganizationMV org =
                    organizationMVRepository
                            .findByBranchId(mapping.getBranchId())
                            .orElse(null);

            if (org == null) {
                continue;
            }

            BranchAccessDTO dto = new BranchAccessDTO();
            dto.setId(mapping.getBranchId());
            dto.setName(org.getBranchName());

            branchAccess.add(dto);
        }

        UserContextResponseDTO response =
                new UserContextResponseDTO();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setRole(roleDTO);
        response.setBranchAccess(branchAccess);
        response.setPermissions(permissions);
        response.setScope("ASSIGNED_BRANCHES");

        return response;
    }

    @Override
    public List<BranchAccessDTO> getAccessibleBranches(Long userId) {

        List<UserBranchMap> mappings =
                userBranchMapRepository.findByUserIdAndStatus(
                        userId,
                        "A");

        List<BranchAccessDTO> branchAccess =
                new ArrayList<>();

        for (UserBranchMap mapping : mappings) {

            OrganizationMV org =
                    organizationMVRepository
                            .findByBranchId(mapping.getBranchId())
                            .orElse(null);

            if (org == null) {
                continue;
            }

            BranchAccessDTO dto = new BranchAccessDTO();
            dto.setId(mapping.getBranchId());
            dto.setName(org.getBranchName());

            branchAccess.add(dto);
        }

        return branchAccess;
    }
}