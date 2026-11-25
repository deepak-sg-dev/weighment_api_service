package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.Projection.RetailScheduleReportProjection;
import com.suguna.weighment_api_service.admin.dtos.requests.RetailScheduleFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.RetailScheduleReportResponse;
import com.suguna.weighment_api_service.admin.entities.BirdsLiftingOrderEntity;
import com.suguna.weighment_api_service.admin.repositories.RetailScheduleReportRepository;
import com.suguna.weighment_api_service.admin.services.RetailScheduleReportService;
import com.suguna.weighment_api_service.admin.specifications.ReportScheduleSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetailScheduleReportServiceImpl implements RetailScheduleReportService {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter DOWNLOAD_TIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final int ORACLE_IN_CLAUSE_LIMIT = 999;

    private final RetailScheduleReportRepository retailScheduleReportRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RetailScheduleReportResponse> getRetailScheduleReport(RetailScheduleFilterRequest filter) {
        if (filter.getFromDate() != null && filter.getToDate() != null
                && filter.getFromDate().isAfter(filter.getToDate())) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }

        // 1. Execute dynamic Specification on SUG_MAI_BIRDS_LIFTING_ORDER
        Specification<BirdsLiftingOrderEntity> spec = ReportScheduleSpecification.withFilters(filter);
        List<BirdsLiftingOrderEntity> schedules = retailScheduleReportRepository.findAll(spec);

        if (schedules.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Fetch joined Branch, Supervisor, Retail Order & Customer Master rows in safe batches
        List<String> erpIndentIds = schedules.stream()
                .map(BirdsLiftingOrderEntity::getErpIndentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, List<RetailScheduleReportProjection>> retailDetailsMap = new HashMap<>();
        for (int i = 0; i < erpIndentIds.size(); i += ORACLE_IN_CLAUSE_LIMIT) {
            List<String> batch = erpIndentIds.subList(i, Math.min(i + ORACLE_IN_CLAUSE_LIMIT, erpIndentIds.size()));
            List<RetailScheduleReportProjection> projections =
                    retailScheduleReportRepository.findRetailDetailsByErpIndentIds(batch);

            for (RetailScheduleReportProjection proj : projections) {
                retailDetailsMap
                        .computeIfAbsent(proj.getErpSchCode(), k -> new ArrayList<>())
                        .add(proj);
            }
        }

        // 3. Combine schedule entity data + retail order detail rows into final API response
        List<RetailScheduleReportResponse> responseList = new ArrayList<>();
        for (BirdsLiftingOrderEntity schedule : schedules) {
            List<RetailScheduleReportProjection> details = retailDetailsMap.get(schedule.getErpIndentId());
            if (details == null || details.isEmpty()) {
                continue; // Only include schedules linked to Retail Orders (INNER JOIN behavior)
            }

            for (RetailScheduleReportProjection detail : details) {
                responseList.add(RetailScheduleReportResponse.builder()
                        .scheduleDate(schedule.getCreationDate() != null
                                ? schedule.getCreationDate().format(DATE_TIME_FMT)
                                : null)
                        .branchName(detail.getBranchName())
                        .supervisor(detail.getSupervisor())
                        .farmName(schedule.getFarmerName())
                        .customerCode(detail.getCustomerCode())
                        .customerName(detail.getCustomerName())
                        .customerAddress(detail.getCustomerAddress())
                        .weight(detail.getWeight())
                        .quantity(detail.getQuantity())
                        .deliveryStatus(detail.getDeliveryStatus())
                        .deliveryDate(detail.getDeliveryDate())
                        .downloadLog(formatDownloadLog(schedule))
                        .erpSchCode(schedule.getErpIndentId())
                        .status(schedule.getLiftingStatus())
                        .build());
            }
        }

        return responseList;
    }

    private String formatDownloadLog(BirdsLiftingOrderEntity schedule) {
        if ("Y".equalsIgnoreCase(schedule.getDownloadFlag())) {
            if (schedule.getDownloadedDate() != null) {
                return "Downloaded on " + schedule.getDownloadedDate().format(DOWNLOAD_TIME_FMT);
            }
            return "Downloaded";
        }
        return "Not Downloaded";
    }
}
