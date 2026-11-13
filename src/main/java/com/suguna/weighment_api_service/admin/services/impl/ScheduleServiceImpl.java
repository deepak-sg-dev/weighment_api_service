package com.suguna.weighment_api_service.admin.services.impl;

import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.ScheduleItemDTO;
import com.suguna.weighment_api_service.admin.dtos.responses.ScheduleListResponseDTO;
import com.suguna.weighment_api_service.admin.repositories.ScheduleRepository;
import com.suguna.weighment_api_service.admin.services.ScheduleService;
import com.suguna.weighment_api_service.admin.specifications.ScheduleSpecification;
import com.suguna.weighment_api_service.entity.SugMaiBirdsLiftingOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl
        implements ScheduleService {

    @Autowired
    private ScheduleRepository repository;

    @Override
    public ScheduleListResponseDTO getSchedules(
            Integer page,
            Integer limit,
            String search,
            Long branchId,
            String supervisorId,
            String traderId,
            String vehicleId,
            String status) {

        Pageable pageable =
                PageRequest.of(
                        page - 1,
                        limit);

        Page<SugMaiBirdsLiftingOrder> schedules =
                repository.findAll(
                        ScheduleSpecification.filter(
                                branchId,
                                supervisorId,
                                traderId,
                                vehicleId,
                                status,
                                search),
                        pageable);

        List<ScheduleItemDTO> items =
                schedules.getContent()
                        .stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());

        PaginationDTO pagination =
                new PaginationDTO();

        pagination.setPage(page);

        pagination.setLimit(limit);

        pagination.setTotal(
                schedules.getTotalElements());

        ScheduleListResponseDTO response =
                new ScheduleListResponseDTO();

        response.setItems(items);

        response.setPagination(
                pagination);

        return response;
    }

    private ScheduleItemDTO mapToDTO(
            SugMaiBirdsLiftingOrder order) {

        ScheduleItemDTO dto =
                new ScheduleItemDTO();

        dto.setId(
                order.getErpindentid());

        dto.setScheduleNo(
                order.getMobileOrderNo() == null
                        ? null
                        : order.getMobileOrderNo().longValue());

        dto.setBranchId(
                order.getBranchid() == null
                        ? null
                        : order.getBranchid().longValue());

        dto.setTraderId(
                order.getCustomercode());

        dto.setVehicleNumber(
                order.getVehicleno());

        dto.setFarmerId(
                order.getFarmercode());

        dto.setFarmerName(
                order.getFarmername());

        dto.setQuantity(
                order.getNoOfKgs() == null
                        ? null
                        : order.getNoOfKgs().doubleValue());

        dto.setPickupDate(
                order.getOrderRequestDate() == null
                        ? null
                        : order.getOrderRequestDate().toString());

        dto.setStatus(
                order.getLiftingStatus());

        return dto;
    }
}
