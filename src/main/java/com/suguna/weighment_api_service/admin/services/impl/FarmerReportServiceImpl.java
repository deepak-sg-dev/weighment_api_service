package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.Projection.FarmerReportProjection;
import com.suguna.weighment_api_service.admin.dtos.requests.FarmerReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerReportListResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerReportResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.repositories.FarmerReportRepository;
import com.suguna.weighment_api_service.admin.services.FarmerReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmerReportServiceImpl implements FarmerReportService {

    private final FarmerReportRepository farmerReportRepository;

    @Override
    @Transactional(readOnly = true)
    public FarmerReportListResponse getFarmerReport(FarmerReportFilterRequest filter) {
        if (filter.getFromDate() != null && filter.getToDate() != null
                && filter.getFromDate().isAfter(filter.getToDate())) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }

        int currentPage = (filter.getPage() != null && filter.getPage() >= 1) ? filter.getPage() : 1;
        int currentLimit = (filter.getLimit() != null && filter.getLimit() > 0) ? filter.getLimit() : 20;
        Pageable pageable = PageRequest.of(currentPage - 1, currentLimit);

        Page<FarmerReportProjection> projectionPage =
                farmerReportRepository.findFarmerReport(
                        filter.getFromDate(),
                        filter.getToDate(),
                        filter.getBranchCode(),
                        filter.getFarmCode(),
                        pageable
                );

        List<FarmerReportResponse> items = projectionPage.getContent()
                .stream()
                .map(proj -> FarmerReportResponse.builder()
                        .site(proj.getSite())
                        .customer(proj.getCustomer())
                        .supervisor(proj.getSupervisor())
                        .dcNo(proj.getDcNo())
                        .startDate(proj.getStartDate())
                        .startTime(proj.getStartTime())
                        .endTime(proj.getEndTime())
                        .vehicleNo(proj.getVehicleNo())
                        .weight(proj.getWeight())
                        .birds(proj.getBirds())
                        .avgWeight(proj.getAvgWeight())
                        .errCount(proj.getErrCount())
                        .schDate(proj.getSchDate())
                        .build())
                .toList();

        PaginationDTO pagination = new PaginationDTO();
        pagination.setPage(currentPage);
        pagination.setLimit(currentLimit);
        pagination.setTotal(projectionPage.getTotalElements());

        return FarmerReportListResponse.builder()
                .items(items)
                .pagination(pagination)
                .build();
    }
}
