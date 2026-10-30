package com.suguna.weighment_api_service.admin.repositories;



import com.suguna.weighment_api_service.admin.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {

    Optional<AppUser> findByUserName(String userName);

    List<AppUser> findByStatus(String status);

    List<AppUser> findByApplicationAndStatus(String application, String status);

    Optional<AppUser> findByUserNameAndApplication(String userName, String application);

    List<AppUser> findByIdInAndStatus(List<Long> id,String status);

    @Query("""
       SELECT u.orgId
       FROM AppUser u
       WHERE u.userId = :userId
       """)
    List<Long> findOrgIdsByUserId(
            @Param("userId") Long userId
    );

    @Query
    List<AppUser> findByOrgIdAndApplicationAndUserTypeAndStatus(Long orgId,String application,String userType,String status);

    @Query
    AppUser findFirstByOrgIdAndApplicationAndUserTypeAndStatusOrderByCreatedAtAsc(
            Long orgId,
            String application,
            String userType,
            String status);

    //Optional<AppUser> findById(Long id);
}

