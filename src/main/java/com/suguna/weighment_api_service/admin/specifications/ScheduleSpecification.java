package com.suguna.weighment_api_service.admin.specifications;

import com.suguna.weighment_api_service.entity.SugMaiBirdsLiftingOrder;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ScheduleSpecification {

    private ScheduleSpecification() {
    }

    public static Specification<SugMaiBirdsLiftingOrder> filter(
            Long branchId,
            String supervisorId,
            String traderId,
            String vehicleId,
            String status,
            String search) {

        return Specification
                .where(applicationFilter())
                .and(branchFilter(branchId))
                .and(supervisorFilter(supervisorId))
                .and(traderFilter(traderId))
                .and(vehicleFilter(vehicleId))
                .and(statusFilter(status))
                .and(searchFilter(search));
    }

    private static Specification<SugMaiBirdsLiftingOrder>
    applicationFilter() {

        return (root, query, cb) ->
                cb.equal(
                        cb.upper(root.get("application")),
                        "BRO-WEIGHMENT");
    }

    private static Specification<SugMaiBirdsLiftingOrder>
    branchFilter(Long branchId) {

        if (branchId == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) ->
                cb.equal(
                        root.get("branchid"),
                        BigDecimal.valueOf(branchId));
    }

    private static Specification<SugMaiBirdsLiftingOrder>
    supervisorFilter(String supervisorId) {

        if (supervisorId == null ||
                supervisorId.trim().isEmpty()) {

            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) ->
                cb.equal(
                        root.get("mobileuserempid"),
                        supervisorId);
    }

    private static Specification<SugMaiBirdsLiftingOrder>
    traderFilter(String traderId) {

        if (traderId == null ||
                traderId.trim().isEmpty()) {

            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) ->
                cb.equal(
                        root.get("customercode"),
                        traderId);
    }

    private static Specification<SugMaiBirdsLiftingOrder>
    vehicleFilter(String vehicleId) {

        if (vehicleId == null ||
                vehicleId.trim().isEmpty()) {

            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) ->
                cb.like(
                        cb.upper(root.get("vehicleno")),
                        "%" + vehicleId.toUpperCase() + "%");
    }

    private static Specification<SugMaiBirdsLiftingOrder>
    statusFilter(String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) ->
                cb.equal(
                        cb.upper(root.get("liftingStatus")),
                        status.toUpperCase());
    }

    private static Specification<SugMaiBirdsLiftingOrder>
    searchFilter(String search) {

        if (search == null ||
                search.trim().isEmpty()) {

            return (root, query, cb) -> cb.conjunction();
        }

        String keyword =
                "%" + search.trim().toUpperCase() + "%";

        return (root, query, cb) ->
                cb.or(
                        cb.like(
                                cb.upper(root.get("erpindentid")),
                                keyword),

                        cb.like(
                                cb.upper(root.get("farmername")),
                                keyword),

                        cb.like(
                                cb.upper(root.get("vehicleno")),
                                keyword),

                        cb.like(
                                cb.upper(root.get("cusName")),
                                keyword)
                );
    }
}