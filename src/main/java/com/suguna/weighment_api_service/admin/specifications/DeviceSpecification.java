package com.suguna.weighment_api_service.admin.specifications;

import com.suguna.weighment_api_service.admin.entities.Device;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class DeviceSpecification {

    private DeviceSpecification() {
    }

    public static Specification<Device> filter(
            String application,
            Long branchId,
            String search) {

        return Specification
                .where(applicationFilter(application))
                .and(branchFilter(branchId))
                .and(searchFilter(search));
    }

    private static Specification<Device> applicationFilter(
            String application) {

        return (root, query, cb) ->
                cb.equal(
                        cb.upper(root.get("application")),
                        application.toUpperCase());
    }

    private static Specification<Device> branchFilter(
            Long branchId) {

        if (branchId == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) ->
                cb.equal(
                        root.get("organizationId"),
                        BigDecimal.valueOf(branchId));
    }

    private static Specification<Device> searchFilter(
            String search) {

        if (search == null || search.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String keyword =
                "%" + search.trim().toLowerCase() + "%";

        return (root, query, cb) ->
                cb.or(

                        cb.like(
                                cb.lower(root.get("androidId")),
                                keyword),

                        cb.like(
                                root.get("deviceId")
                                        .as(String.class),
                                "%" + search.trim() + "%"),

                        cb.like(
                                cb.lower(root.get("empCode")),
                                keyword),

                        cb.like(
                                root.get("empId")
                                        .as(String.class),
                                "%" + search.trim() + "%")
                );
    }
}