package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.RoleMenuAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleMenuAccessRepository
        extends JpaRepository<RoleMenuAccess, Long> {

    List<RoleMenuAccess> findByRoleIdAndStatus(
            Long roleId,
            String status);
}