package com.suguna.weighment_api_service.admin.specifications;

import com.suguna.weighment_api_service.admin.dtos.requests.RetailScheduleFilterRequest;
import com.suguna.weighment_api_service.admin.entities.BirdsLiftingOrderEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class ReportScheduleSpecification {

    private ReportScheduleSpecification() {
    }

    public static Specification<BirdsLiftingOrderEntity> withFilters(RetailScheduleFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Date Range Filter
            if (filter.getFromDate() != null && filter.getToDate() != null) {
                predicates.add(cb.between(
                        root.get("creationDate"),
                        filter.getFromDate().atStartOfDay(),
                        filter.getToDate().atTime(LocalTime.MAX)
                ));
            } else if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("creationDate"),
                        filter.getFromDate().atStartOfDay()
                ));
            } else if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("creationDate"),
                        filter.getToDate().atTime(LocalTime.MAX)
                ));
            }

            // 2. NPick Schedule No Filter
            if (StringUtils.hasText(filter.getNpickScheduleNo())) {
                predicates.add(cb.equal(
                        cb.trim(root.get("scheduleNo").as(String.class)),
                        filter.getNpickScheduleNo().trim()
                ));
            }

            // 3. ERP Schedule No Filter
            if (StringUtils.hasText(filter.getErpScheduleNo())) {
                predicates.add(cb.equal(
                        cb.trim(root.get("erpIndentId").as(String.class)),
                        filter.getErpScheduleNo().trim()
                ));
            }

            // 4. Status Radio Filter: All | Completed | Pending | Not Downloaded
            if (StringUtils.hasText(filter.getStatus()) && !"All".equalsIgnoreCase(filter.getStatus().trim())) {
                String status = filter.getStatus().trim();
                if ("Not Downloaded".equalsIgnoreCase(status)) {
                    predicates.add(cb.equal(
                            cb.coalesce(root.get("downloadFlag"), "N"),
                            "N"
                    ));
                } else {
                    predicates.add(cb.equal(
                            cb.upper(root.get("liftingStatus")),
                            status.toUpperCase()
                    ));
                }
            }

            // 5. Branch / Region Filter
            if (StringUtils.hasText(filter.getBranchCode())) {
                predicates.add(cb.like(
                        cb.upper(root.get("branchCode")),
                        filter.getBranchCode().trim().toUpperCase() + "%"
                ));
            }

            if (query != null && Long.class != query.getResultType()) {
                query.orderBy(
                        cb.desc(root.get("creationDate")),
                        cb.asc(root.get("erpIndentId"))
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
