package com.suguna.weighment_api_service.admin.repositories;



import com.suguna.weighment_api_service.admin.entities.RoleMenuAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleMenuAccessRepository extends JpaRepository<RoleMenuAccess, Long> {

    List<RoleMenuAccess> findByRoleIdAndStatus(Long roleId, String status);

    List<RoleMenuAccess> findByRoleId(Long roleId);

    List<RoleMenuAccess> findByRoleIdInAndStatus(List<Long> roleIds, String status);

    Optional<RoleMenuAccess> findByRoleIdAndMenuId(Long roleId, Long menuId);

}
