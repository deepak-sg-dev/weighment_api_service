package com.suguna.weighment_api_service.admin.models;



import com.suguna.weighment_api_service.admin.entities.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private Long userId;
    private String username;
    private String password;
    private boolean enabled;

    public CustomUserDetails(AppUser user) {
        this.userId = user.getId();
        this.username = user.getUserName();
        this.password = user.getPassword();
        this.enabled = "A".equals(user.getStatus());
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>(); // roles later
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

