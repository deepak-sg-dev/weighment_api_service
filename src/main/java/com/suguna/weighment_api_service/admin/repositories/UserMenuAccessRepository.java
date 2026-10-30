package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.UserMenuAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserMenuAccessRepository
        extends JpaRepository<UserMenuAccess, Long> {

    List<UserMenuAccess> findByUserIdAndStatus(
            Long userId,
            String status);
}