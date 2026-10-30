package com.suguna.weighment_api_service.admin.repositories;

import com.suguna.weighment_api_service.admin.entities.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VehicleRepository
        extends JpaRepository<Vehicle, String> {

    List<Vehicle> findByOperatingUnit(
            BigDecimal operatingUnit);
}