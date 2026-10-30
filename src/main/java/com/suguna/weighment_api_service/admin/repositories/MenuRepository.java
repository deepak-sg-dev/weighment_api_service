package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository
        extends JpaRepository<Menu, Long> {

    List<Menu> findByApplicationAndStatus(
            String application,
            String status);
}