package com.suguna.weighment_api_service.admin.specifications;

import com.suguna.weighment_api_service.admin.entities.Supervisor;
import org.springframework.data.jpa.domain.Specification;

public class SupervisorSpecification {

    private SupervisorSpecification() {
    }

    public static Specification<Supervisor> filter(
            String application,
            Long branchId,
            String status,
            String search) {

        return Specification
                .where(applicationFilter(application))
                .and(branchFilter(branchId))
                .and(statusFilter(status))
                .and(searchFilter(search));
    }

    private static Specification<Supervisor> applicationFilter(String application) {
        return (root, query, cb) ->
                cb.equal(
                        cb.upper(root.get("application")),
                        application.toUpperCase());
    }

    private static Specification<Supervisor> branchFilter(Long branchId) {
        if (branchId == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.equal(root.get("organizationId"), branchId);
    }

    private static Specification<Supervisor> statusFilter(String status) {
        if (status == null || status.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) ->
                cb.equal(
                        cb.upper(root.get("status")),
                        status.trim().toUpperCase());
    }

    private static Specification<Supervisor> searchFilter(String search) {
        if (search == null || search.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String keyword = "%" + search.trim().toLowerCase() + "%";

        return (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("empName")), keyword),
                        cb.like(cb.lower(root.get("empCode")), keyword),
                        cb.like(cb.lower(root.get("contactNumber")), keyword),
                        cb.like(cb.lower(root.get("deviceId").as(String.class)), keyword)
                );
    }
}
