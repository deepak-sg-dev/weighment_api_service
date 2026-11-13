package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserListItemDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserListResponseDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.UserSummaryDTO;
import com.suguna.weighment_api_service.admin.entities.AppUser;
import com.suguna.weighment_api_service.admin.entities.Role;
import com.suguna.weighment_api_service.admin.repositories.AppUserRepository;
import com.suguna.weighment_api_service.admin.repositories.RoleRepository;
import com.suguna.weighment_api_service.admin.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final String APP_NAME = "WMT_APP";

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public UserListResponseDTO getUsers(
            Integer page,
            Integer limit,
            String search,
            String userType,
            Long roleId,
            String status) {

        List<AppUser> allUsers =
                userRepository.findByApplicationOrderByIdDesc(APP_NAME);

        // 1. Summary cards across all WMT_APP users
        UserSummaryDTO summary = new UserSummaryDTO();
        summary.setTotalUsers(allUsers.size());
        summary.setActiveUsers(
                allUsers.stream()
                        .filter(u -> "A".equalsIgnoreCase(u.getStatus()))
                        .count());
        summary.setEmployeesAndStaff(
                allUsers.stream()
                        .filter(u -> u.getUserType() != null &&
                                (u.getUserType().equalsIgnoreCase("EMPLOYEE")
                                        || u.getUserType().equalsIgnoreCase("STAFF")
                                        || u.getUserType().equalsIgnoreCase("ADMIN")))
                        .count());
        summary.setPartnersAndCustomers(
                allUsers.stream()
                        .filter(u -> u.getUserType() != null &&
                                (u.getUserType().equalsIgnoreCase("PARTNER")
                                        || u.getUserType().equalsIgnoreCase("CUSTOMER")))
                        .count());

        // 2. Map role names in bulk
        Map<Long, String> roleMap = roleRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        Role::getId,
                        Role::getRoleName,
                        (a, b) -> a));

        // 3. Apply search, userType, roleId, and status filters
        String searchLower = (search != null && !search.isBlank())
                ? search.trim().toLowerCase()
                : null;

        List<UserListItemDTO> filtered = allUsers.stream()
                .filter(u -> userType == null || userType.isBlank()
                        || "ALL".equalsIgnoreCase(userType)
                        || userType.equalsIgnoreCase(u.getUserType()))
                .filter(u -> roleId == null
                        || roleId.equals(u.getRoleId()))
                .filter(u -> {
                    if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
                        return true;
                    }
                    if ("ACTIVE".equalsIgnoreCase(status) || "A".equalsIgnoreCase(status)) {
                        return "A".equalsIgnoreCase(u.getStatus());
                    }
                    if ("INACTIVE".equalsIgnoreCase(status) || "I".equalsIgnoreCase(status)) {
                        return "I".equalsIgnoreCase(u.getStatus());
                    }
                    return status.equalsIgnoreCase(u.getStatus());
                })
                .filter(u -> {
                    if (searchLower == null) {
                        return true;
                    }
                    boolean matchUser = u.getUserName() != null
                            && u.getUserName().toLowerCase().contains(searchLower);
                    boolean matchName = u.getName() != null
                            && u.getName().toLowerCase().contains(searchLower);
                    boolean matchEmail = u.getEmail() != null
                            && u.getEmail().toLowerCase().contains(searchLower);
                    boolean matchMobile = u.getMobile() != null
                            && String.valueOf(u.getMobile()).contains(searchLower);
                    return matchUser || matchName || matchEmail || matchMobile;
                })
                .map(u -> mapToDto(u, roleMap.get(u.getRoleId())))
                .toList();

        // 4. Paginate results
        long total = filtered.size();
        int fromIndex = Math.min((page - 1) * limit, filtered.size());
        int toIndex = Math.min(fromIndex + limit, filtered.size());
        List<UserListItemDTO> pagedItems = filtered.subList(fromIndex, toIndex);

        PaginationDTO pagination = new PaginationDTO();
        pagination.setPage(page);
        pagination.setLimit(limit);
        pagination.setTotal(total);

        UserListResponseDTO response = new UserListResponseDTO();
        response.setSummary(summary);
        response.setItems(pagedItems);
        response.setPagination(pagination);

        return response;
    }

    @Override
    public UserListItemDTO getUserById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!APP_NAME.equalsIgnoreCase(user.getApplication())) {
            throw new RuntimeException("User not found");
        }

        String roleName = user.getRoleId() != null
                ? roleRepository.findById(user.getRoleId())
                .map(Role::getRoleName)
                .orElse(null)
                : null;

        return mapToDto(user, roleName);
    }

    private UserListItemDTO mapToDto(AppUser user, String roleName) {
        UserListItemDTO dto = new UserListItemDTO();
        dto.setId(user.getId());
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUserName());
        dto.setName(user.getName());
        dto.setMobile(
                user.getMobile() != null
                        ? String.valueOf(user.getMobile())
                        : null);
        dto.setEmail(user.getEmail());
        dto.setOrgId(user.getOrgId());
        dto.setBranchId(user.getBranchId());
        dto.setUserType(user.getUserType());
        dto.setApplication(user.getApplication());
        dto.setRoleId(user.getRoleId());
        dto.setRoleName(roleName);
        dto.setStatus(
                "A".equalsIgnoreCase(user.getStatus())
                        ? "ACTIVE"
                        : "INACTIVE");
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
