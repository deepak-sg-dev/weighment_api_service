package com.suguna.weighment_api_service.admin.specifications;

import com.suguna.weighment_api_service.admin.dtos.requests.RetailScheduleFilterRequest;
import com.suguna.weighment_api_service.admin.entities.BirdsLiftingOrderEntity_1;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class ReportScheduleSpecification_1 {

    private ReportScheduleSpecification_1() {
    }

    public static Specification<BirdsLiftingOrderEntity_1> withFilters(RetailScheduleFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Indexed DI_CREATION_DATE window + exact CREATION_DATE filter
            if (filter.getFromDate() != null && filter.getToDate() != null) {
                predicates.add(cb.between(
                        root.get("diCreationDate"),
                        filter.getFromDate().minusDays(1).atStartOfDay(),
                        filter.getToDate().plusDays(1).atTime(LocalTime.MAX)
                ));
                predicates.add(cb.between(
                        root.get("creationDate"),
                        filter.getFromDate().atStartOfDay(),
                        filter.getToDate().atTime(LocalTime.MAX)
                ));
            } else if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("diCreationDate"),
                        filter.getFromDate().minusDays(1).atStartOfDay()
                ));
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("creationDate"),
                        filter.getFromDate().atStartOfDay()
                ));
            } else if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("diCreationDate"),
                        filter.getToDate().plusDays(1).atTime(LocalTime.MAX)
                ));
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("creationDate"),
                        filter.getToDate().atTime(LocalTime.MAX)
                ));
            }

            // 2. NPick Schedule No (MOBILE_ORDER_NO - indexed)
            if (StringUtils.hasText(filter.getNpickScheduleNo())) {
                predicates.add(cb.equal(
                        root.get("mobileOrderNo"),
                        filter.getNpickScheduleNo().trim()
                ));
            }

            // 3. ERP Schedule No (ERPINDENTID - primary key)
            if (StringUtils.hasText(filter.getErpScheduleNo())) {
                predicates.add(cb.equal(
                        root.get("erpIndentId"),
                        filter.getErpScheduleNo().trim()
                ));
            }

            // 4. Status Radio Filter
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

            // 5. Branch Filter
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
