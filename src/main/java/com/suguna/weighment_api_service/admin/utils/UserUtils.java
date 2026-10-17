package com.suguna.weighment_api_service.admin.utils;


import com.suguna.weighment_api_service.admin.entities.AppUser;
import com.suguna.weighment_api_service.admin.models.CustomUserDetails;
import com.suguna.weighment_api_service.admin.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class UserUtils {


    @Autowired
    private AppUserRepository userRepository;


    // ✅ Get full Authentication object
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }


    public boolean isPasswordExpired(AppUser user) {

        if (user.getPasswordChangedAt() == null ||
                user.getPasswordExpiryDays() == null) {
            return false;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(user.getPasswordChangedAt());
        cal.add(Calendar.DAY_OF_MONTH, user.getPasswordExpiryDays());

        Date expiryDate = cal.getTime();

        return new Date().after(expiryDate);
    }

    public AppUser getUser(AppUserRepository userRepository) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails user =
                (CustomUserDetails) auth.getPrincipal();

        return userRepository.findById(user.getUserId())
                .orElseThrow();

    }


    // ✅ Get CustomUserDetails safely
    private CustomUserDetails getUserDetails() {

        Authentication auth = getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails user) {
            return user;
        }

        throw new RuntimeException("Invalid user principal");
    }


    // ✅ Get Logged-in User ID
    public Long getCurrentUserId() {
        return getUserDetails().getUserId();
    }

    // ✅ Get Username
    public String getCurrentUsername() {
        return getUserDetails().getUsername();
    }

    // ✅ Get Full User Entity (from DB)
    public AppUser getCurrentUser() {
        Long userId = getCurrentUserId();

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ Get OrgId (used in your Items/Indent logic)
    public Long getCurrentUserOrgId() {
        return getCurrentUser().getOrgId();
    }

    // ✅ Get RoleId
    public Long getCurrentUserRoleId() {
        return getCurrentUser().getRoleId();
    }

    // ✅ Get UserType (EMPLOYEE / FRANCHISE / etc.)
    public String getCurrentUserType() {
        return getCurrentUser().getUserType();
    }


}

