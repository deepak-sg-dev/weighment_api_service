package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.UserMenuAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMenuAccessRepository extends JpaRepository<UserMenuAccess, Long> {

    List<UserMenuAccess> findByUserIdAndStatus(Long userId, String status);

    Optional<UserMenuAccess> findByUserIdAndMenuId(Long userId, Long menuId);

    List<UserMenuAccess> findByUserIdAndMenuIdInAndStatus(
            Long userId, List<Long> menuIds, String status);
}

