package com.suguna.weighment_api_service.admin.specifications;

import com.suguna.weighment_api_service.entity.SugMaiBirdsLiftingOrder;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleSpecification {

    private ScheduleSpecification() {
    }

    public static Specification<SugMaiBirdsLiftingOrder> filter(
            Long branchId,
            String supervisorId,
            String traderId,
            String depot,
            String vehicleId,
            String pickupDate,
            String status,
            String search) {

        return Specification
                .where(applicationFilter())
                .and(sourceFilter())
                .and(branchFilter(branchId))
                .and(supervisorFilter(supervisorId))
                .and(traderFilter(traderId))
                .and(depotFilter(depot))
                .and(vehicleFilter(vehicleId))
                .and(pickupDateFilter(pickupDate))
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
    sourceFilter() {

        return (root, query, cb) ->
                cb.upper(root.get("source"))
                        .in(List.of("MOBILE", "RETAIL"));
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
                        supervisorId.trim());
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
                        traderId.trim());
    }

    private static Specification<SugMaiBirdsLiftingOrder>
    depotFilter(String depot) {

        if (depot == null ||
                depot.trim().isEmpty()) {

            return (root, query, cb) -> cb.conjunction();
        }

        return (root, query, cb) ->
                cb.like(
                        cb.upper(root.get("cusLocation")),
                        "%" + depot.trim().toUpperCase() + "%");
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
                        "%" + vehicleId.trim().toUpperCase() + "%");
    }

    private static Specification<SugMaiBirdsLiftingOrder>
    pickupDateFilter(String pickupDate) {

        if (pickupDate == null ||
                pickupDate.trim().isEmpty()) {

            return (root, query, cb) -> cb.conjunction();
        }

        LocalDate date = parseDate(pickupDate.trim());
        if (date == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return (root, query, cb) ->
                cb.and(
                        cb.greaterThanOrEqualTo(root.get("orderRequestDate"), startOfDay),
                        cb.lessThan(root.get("orderRequestDate"), endOfDay)
                );
    }

    private static LocalDate parseDate(String value) {
        for (DateTimeFormatter fmt : List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"))) {
            try {
                return LocalDate.parse(value, fmt);
            } catch (Exception ignored) {
            }
        }
        return null;
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
                        status.trim().toUpperCase());
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
                                cb.upper(root.get("customercode")),
                                keyword),

                        cb.like(
                                cb.upper(root.get("cusName")),
                                keyword),

                        cb.like(
                                cb.upper(root.get("farmername")),
                                keyword),

                        cb.like(
                                cb.upper(root.get("vehicleno")),
                                keyword)
                );
    }
}
