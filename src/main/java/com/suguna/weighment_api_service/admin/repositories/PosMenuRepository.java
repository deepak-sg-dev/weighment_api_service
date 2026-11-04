package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.PosMenu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Full menu CRUD for {@link PosMenu}; {@link MenuRepository} keeps the legacy {@link com.suguna.weighment_api_service.admin.entities.Menu} read model. */
public interface PosMenuRepository extends JpaRepository<PosMenu, Long> {

    List<PosMenu> findByIsDeleted(int isDeleted);

    List<PosMenu> findByApplicationAndStatusAndIsDeletedOrderBySequenceAsc(
            String application, String status, int isDeleted);

    List<PosMenu> findByParentIdAndIsDeleted(long parentId, int isDeleted);
}
