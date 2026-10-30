package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.requests.LoginRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.CurrentUserResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.LoginResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.MenuDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserDTO;
import com.suguna.weighment_api_service.admin.entities.AppUser;
import com.suguna.weighment_api_service.admin.entities.Menu;
import com.suguna.weighment_api_service.admin.entities.Role;
import com.suguna.weighment_api_service.admin.entities.RoleMenuAccess;
import com.suguna.weighment_api_service.admin.repositories.AppUserRepository;
import com.suguna.weighment_api_service.admin.repositories.MenuRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleMenuAccessRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleRepository;
import com.suguna.weighment_api_service.admin.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleMenuAccessRepository roleMenuAccessRepository;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {

        AppUser user = userRepository
                .findByUserNameAndApplication(
                        request.getUsername(),
                        "WMT_APP")
                .orElseThrow(() ->
                        new RuntimeException("Invalid Username"));

        if (!"A".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("User is inactive");
        }

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        if (!encoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid Password");
        }

        Role role = roleRepository
                .findById(user.getRoleId())
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        UserDTO userDto = new UserDTO();

        userDto.setId(
                String.valueOf(user.getId()));

        userDto.setRole(
                role.getRoleName());

        userDto.setPermissions(
                List.of(
                        "schedule.read",
                        "reports.read"
                ));

        LoginResponseDTO response =
                new LoginResponseDTO();

        response.setAccessToken(
                "TEMP_TOKEN");

        response.setUser(
                userDto);

        response.setExpiresIn(
                3600L);

        return response;
    }

    @Override
    public CurrentUserResponseDTO getCurrentUser(
            Long userId) {

        AppUser user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        Role role = roleRepository
                .findById(user.getRoleId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found"));

        CurrentUserResponseDTO response =
                new CurrentUserResponseDTO();

        response.setUserId(
                user.getId());

        response.setUsername(
                user.getUserName());

        response.setName(
                user.getName());

        response.setRoleId(
                role.getId());

        response.setRoleName(
                role.getRoleName());

        response.setOrgId(
                user.getOrgId());

        response.setUserType(
                user.getUserType());

        if (user.getMobile() != null) {

            response.setMobileNumber(
                    String.valueOf(
                            user.getMobile()));
        }

        response.setStatus(
                user.getStatus());

        List<RoleMenuAccess> accesses =
                roleMenuAccessRepository
                        .findByRoleIdAndStatus(
                                role.getId(),
                                "A");

        List<MenuDTO> menus =
                new ArrayList<>();

        for (RoleMenuAccess access : accesses) {

            Menu menu = menuRepository
                    .findById(
                            access.getMenuId())
                    .orElse(null);

            if (menu == null) {
                continue;
            }

            MenuDTO menuDto =
                    new MenuDTO();

            menuDto.setId(
                    menu.getId());

            menuDto.setName(
                    menu.getMenuName());

            menuDto.setPath(
                    menu.getMenuPath());

            menus.add(menuDto);
        }

        response.setMenus(
                menus);

        return response;
    }

    @Override
    public void logout(
            Long userId) {

        userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        // Token invalidation can be implemented later
    }

    @Override
    public void forgotPassword(
            String username) {

        userRepository
                .findByUserNameAndApplication(
                        username,
                        "WMT_APP")
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));

        // Future Implementation:
        // OTP
        // Email
        // Password Reset Link
    }
}