package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.Projection.DailyReportProjection;
import com.suguna.weighment_api_service.admin.dtos.requests.DailyReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.DailyReportListResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.DailyReportResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.repositories.DailyReportRepository;
import com.suguna.weighment_api_service.admin.services.DailyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportServiceImpl implements DailyReportService {

    private final DailyReportRepository dailyReportRepository;

    @Override
    @Transactional(readOnly = true)
    public DailyReportListResponse getDailyReport(DailyReportFilterRequest filter) {
        LocalDate effectiveFrom = filter.getReportDate() != null
                ? filter.getReportDate()
                : (filter.getFromDate() != null ? filter.getFromDate() : LocalDate.now());

        LocalDate effectiveTo = filter.getReportDate() != null
                ? filter.getReportDate()
                : (filter.getToDate() != null ? filter.getToDate() : effectiveFrom);

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }

        int currentPage = (filter.getPage() != null && filter.getPage() >= 1) ? filter.getPage() : 1;
        int currentLimit = (filter.getLimit() != null && filter.getLimit() > 0) ? filter.getLimit() : 20;
        Pageable pageable = PageRequest.of(currentPage - 1, currentLimit);

        Page<DailyReportProjection> projectionPage =
                dailyReportRepository.findDailyReport(
                        effectiveFrom,
                        effectiveTo,
                        filter.getBranchCode(),
                        filter.getFarmCode(),
                        pageable
                );

        List<DailyReportResponse> items = projectionPage.getContent()
                .stream()
                .map(proj -> DailyReportResponse.builder()
                        .schCode(proj.getSchCode())
                        .farmerName(proj.getFarmerName())
                        .farmCode(proj.getFarmCode())
                        .customerName(proj.getCustomerName())
                        .customerCode(proj.getCustomerCode())
                        .supervisorCode(proj.getSupervisorCode())
                        .supervisorName(proj.getSupervisorName())
                        .dcNumber(proj.getDcNumber())
                        .slipNo(proj.getSlipNo())
                        .weighmentStartDate(proj.getWeighmentStartDate())
                        .weighmentStartTime(proj.getWeighmentStartTime())
                        .weighmentEndTime(proj.getWeighmentEndTime())
                        .vehicleNo(proj.getVehicleNo())
                        .weightOfBirds(proj.getWeightOfBirds())
                        .noOfBirds(proj.getNoOfBirds())
                        .birdType(proj.getBirdType())
                        .avgWeight(proj.getAvgWeight())
                        .errorCount(proj.getErrorCount())
                        .abortCount(proj.getAbortCount())
                        .feedStockBag(proj.getFeedStockBag())
                        .vehicleIn(proj.getVehicleIn())
                        .vehicleOut(proj.getVehicleOut())
                        .branchCode(proj.getBranchCode())
                        .schDate(proj.getSchDate())
                        .deliveryStartTime(proj.getDeliveryStartTime())
                        .deliveryEndTime(proj.getDeliveryEndTime())
                        .build())
                .toList();

        PaginationDTO pagination = new PaginationDTO();
        pagination.setPage(currentPage);
        pagination.setLimit(currentLimit);
        pagination.setTotal(projectionPage.getTotalElements());

        return DailyReportListResponse.builder()
                .items(items)
                .pagination(pagination)
                .build();
    }
}
