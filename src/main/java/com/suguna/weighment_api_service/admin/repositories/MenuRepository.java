package com.suguna.weighment_api_service.admin.repositories;


import com.suguna.weighment_api_service.admin.entities.PosMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<PosMenu, Long> {

    List<PosMenu> findByIsDeleted(int isDeleted);

    List<PosMenu> findByStatus(String status);

    List<PosMenu> findByParentId(Long parentId);

    List<PosMenu> findByApplicationAndStatus(String application, String status);

    List<PosMenu> findByIdInAndStatus(List<Long> ids, String status);


    List<PosMenu> findByApplicationAndStatusOrderBySequenceAsc(
            String application,
            String status
    );

    List<PosMenu> findByApplicationAndStatusAndIsDeletedOrderBySequenceAsc(
            String application,
            String status,
            int isDeleted
    );

    List<PosMenu> findByIdInAndStatusOrderBySequenceAsc(
            List<Long> ids,
            String status
    );

    List<PosMenu> findByIdInAndStatusAndIsDeletedOrderBySequenceAsc(
            List<Long> ids,
            String status,
            int isDeleted
    );

    List<PosMenu> findByParentIdAndIsDeleted(Long parentId,int isDeleted);


}

