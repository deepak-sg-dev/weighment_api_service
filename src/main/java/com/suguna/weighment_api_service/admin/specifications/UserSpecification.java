package com.suguna.weighment_api_service.admin.specifications;

import com.suguna.weighment_api_service.admin.dtos.responses.UserFilterDTO;
import com.suguna.weighment_api_service.admin.entities.AppUser;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<AppUser> filterUsers(UserFilterDTO filter) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // ✅ filter by user type
            if (filter.getUserType() != null && !filter.getUserType().isBlank()) {
                predicates.add(cb.equal(root.get("userType"), filter.getUserType()));
            }

            // ✅ filter by role
            if (filter.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), filter.getRole()));
            }

            // ✅ filter by status
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
