package com.suguna.weighment_api_service.admin.repositories;



import com.suguna.weighment_api_service.admin.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findByStatus(String status);

    List<Role> findByApplicationAndStatus(String application, String status);

    Optional<Role> findByRoleNameAndApplication(String roleName, String application);
}

