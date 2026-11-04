package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.UserBranchMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBranchMapRepository
        extends JpaRepository<UserBranchMap, Long> {

    List<UserBranchMap> findByUserIdAndStatus(
            Long userId,
            String status);
}