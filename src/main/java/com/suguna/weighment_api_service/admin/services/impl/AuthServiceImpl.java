package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.requests.ChangePasswordRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.LoginRequestDTO;
import com.suguna.weighment_api_service.admin.dtos.requests.RefreshTokenRequestDTO;
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
import com.suguna.weighment_api_service.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
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

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        AppUser user = userRepository
                .findByUserNameAndApplication(request.getUsername(), "WMT_APP")
                .orElseThrow(() -> new RuntimeException("Invalid Username"));

        if (!"A".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("User is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        // Update LAST_LOGIN_AT in SUG_MAI_USER
        user.setLastLoginAt(new Date());
        userRepository.save(user);

        return buildLoginResponse(user);
    }

    @Override
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String token = request.getRefreshToken();

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Refresh token is required");
        }

        if (!jwtService.isTokenValid(token)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        Long userId = jwtService.extractUserId(token);
        Date tokenIssuedAt = jwtService.extractIssuedAt(token);

        AppUser user = userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"A".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("User is inactive");
        }

        // Reject refresh tokens issued before the last password change
        if (user.getPasswordChangedAt() != null && tokenIssuedAt != null
                && tokenIssuedAt.before(user.getPasswordChangedAt())) {
            throw new RuntimeException("Token invalidated due to password change");
        }

        return buildLoginResponse(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequestDTO request) {
        AppUser user = userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"A".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("User is inactive");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters long");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password cannot be the same as the old password");
        }

        Date now = new Date();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(now);
        user.setUpdatedAt(now);
        user.setUpdatedBy(userId);

        userRepository.save(user);
    }

    private LoginResponseDTO buildLoginResponse(AppUser user) {
        Role role = roleRepository
                .findById(user.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<RoleMenuAccess> accesses = roleMenuAccessRepository
                .findByRoleIdAndStatus(role.getId(), "A");

        List<String> permissions = new ArrayList<>();

        for (RoleMenuAccess access : accesses) {
            Menu menu = menuRepository
                    .findById(access.getMenuId())
                    .orElse(null);

            if (menu == null) {
                continue;
            }

            String module = menu.getMenuName()
                    .trim()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]+", ".");

            module = module.replaceAll("^\\.|\\.$", "");

            if ("retail".equals(module) || "wholesale".equals(module)) {
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

        UserDTO userDto = new UserDTO();
        userDto.setId(String.valueOf(user.getId()));
        userDto.setUserType(user.getUserType());
        userDto.setRole(role.getRoleName());
        userDto.setPermissions(permissions);

        String accessToken = jwtService.generateAdminAccessToken(
                user.getId(), user.getUserName(), role.getRoleName());

        String refreshToken = jwtService.generateAdminRefreshToken(
                user.getId(), user.getUserName());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(28800L);
        response.setUser(userDto);

        return response;
    }

    @Override
    public CurrentUserResponseDTO getCurrentUser(Long userId) {
        AppUser user = userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository
                .findById(user.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        CurrentUserResponseDTO response = new CurrentUserResponseDTO();
        response.setUserId(user.getId());
        response.setUsername(user.getUserName());
        response.setName(user.getName());
        response.setRoleId(role.getId());
        response.setRoleName(role.getRoleName());
        response.setOrgId(user.getOrgId());
        response.setUserType(user.getUserType());

        if (user.getMobile() != null) {
            response.setMobileNumber(String.valueOf(user.getMobile()));
        }

        response.setStatus(user.getStatus());

        List<RoleMenuAccess> accesses = roleMenuAccessRepository
                .findByRoleIdAndStatus(role.getId(), "A");

        List<MenuDTO> menus = new ArrayList<>();

        for (RoleMenuAccess access : accesses) {
            Menu menu = menuRepository
                    .findById(access.getMenuId())
                    .orElse(null);

            if (menu == null) {
                continue;
            }

            MenuDTO menuDto = new MenuDTO();
            menuDto.setId(menu.getId());
            menuDto.setName(menu.getMenuName());
            menuDto.setPath(menu.getMenuPath());

            menus.add(menuDto);
        }

        response.setMenus(menus);
        return response;
    }

    @Override
    public void logout(Long userId) {
        userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void forgotPassword(String username) {
        userRepository
                .findByUserNameAndApplication(username, "WMT_APP")
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
