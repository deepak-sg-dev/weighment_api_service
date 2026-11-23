package com.suguna.weighment_api_service.service;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.domain.LiftingSchedule;
import com.suguna.weighment_api_service.dto.weighment.schedule.PartyContactDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleBranchDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleCustomerDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDcDocumentDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleDeliveryDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleSupervisorDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.ScheduleVehicleDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.WeighmentDcLineDto;
import com.suguna.weighment_api_service.dto.weighment.schedule.WeighmentDcResponse;
import com.suguna.weighment_api_service.dto.weighment.schedule.WeighmentDcTotalsDto;
import com.suguna.weighment_api_service.exception.BadRequestException;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.integration.oracle.ScheduleDcAddressSqlClient;
import com.suguna.weighment_api_service.integration.oracle.ScheduleDcAddressSqlClient.PartyAddress;
import com.suguna.weighment_api_service.integration.oracle.TransactionSqlClient;
import com.suguna.weighment_api_service.integration.oracle.TransactionSqlClient.SubmissionHeaderSnapshot;
import com.suguna.weighment_api_service.integration.oracle.WeighmentDetailSqlClient;
import com.suguna.weighment_api_service.integration.oracle.WeighmentDetailSqlClient.StoredWeighmentDetailLine;
import com.suguna.weighment_api_service.mapper.ScheduleMapper;
import com.suguna.weighment_api_service.repository.ItemGstRepository;
import com.suguna.weighment_api_service.security.AuthenticatedSupervisor;
import com.suguna.weighment_api_service.util.BirdTypeNormalizer;
import com.suguna.weighment_api_service.util.DcItemCodeSupport;
import com.suguna.weighment_api_service.util.RegisteredOfficeAddressSupport;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import com.suguna.weighment_api_service.util.WeighmentSubmitKeys;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class ScheduleDcDocumentService {

    private final ScheduleAccessService scheduleAccessService;
    private final ScheduleRetailEnrichmentService retailEnrichmentService;
    private final ScheduleDcAddressSqlClient scheduleDcAddressSqlClient;
    private final ItemGstRepository itemGstRepository;
    private final OracleErpProperties erpProperties;
    private final TransactionSqlClient transactionSqlClient;
    private final WeighmentDetailSqlClient weighmentDetailSqlClient;

    public ScheduleDcDocumentService(
            ScheduleAccessService scheduleAccessService,
            ScheduleRetailEnrichmentService retailEnrichmentService,
            ScheduleDcAddressSqlClient scheduleDcAddressSqlClient,
            ItemGstRepository itemGstRepository,
            OracleErpProperties erpProperties,
            TransactionSqlClient transactionSqlClient,
            WeighmentDetailSqlClient weighmentDetailSqlClient) {
        this.scheduleAccessService = scheduleAccessService;
        this.retailEnrichmentService = retailEnrichmentService;
        this.scheduleDcAddressSqlClient = scheduleDcAddressSqlClient;
        this.itemGstRepository = itemGstRepository;
        this.erpProperties = erpProperties;
        this.transactionSqlClient = transactionSqlClient;
        this.weighmentDetailSqlClient = weighmentDetailSqlClient;
    }

    public WeighmentDcResponse getLiftingDc(AuthenticatedSupervisor session, String scheduleId) {
        return getLiftingDc(session, scheduleAccessService.requireLifting(session, scheduleId));
    }

    /** Same lifting DC as {@link #getLiftingDc(AuthenticatedSupervisor, String)} when the schedule is already loaded. */
    public WeighmentDcResponse getLiftingDc(AuthenticatedSupervisor session, LiftingSchedule schedule) {
        String saleType = retailEnrichmentService.resolveSaleType(schedule.getOrderSource());
        SubmissionHeaderSnapshot header = transactionSqlClient
                .findSubmissionHeader(schedule.getScheduleId(), liftingSupervisorCode(session, schedule))
                .orElse(null);
        List<StoredWeighmentDetailLine> stored =
                weighmentDetailSqlClient.listLiftingCompleteLines(WeighmentSubmitKeys.transactionKey(schedule.getScheduleId()));
        return buildDc(session, schedule, saleType, WeighmentDcResponse.TYPE_LIFTING, null, null, header, stored);
    }

    public WeighmentDcResponse getDeliveryDc(
            AuthenticatedSupervisor session, String scheduleId, String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new BadRequestException("orderId is required");
        }
        LiftingSchedule schedule = scheduleAccessService.requireLifting(session, scheduleId);
        String saleType = retailEnrichmentService.resolveSaleType(schedule.getOrderSource());
        if (!"RETAIL".equals(saleType)) {
            throw new BadRequestException("Delivery DC is only available for retail schedules");
        }
        String liftingSupervisor = liftingSupervisorCode(session, schedule);
        ScheduleDeliveryDto shop = retailEnrichmentService.loadOneDelivery(liftingSupervisor, schedule, orderId);
        if (shop == null) {
            throw new ResourceNotFoundException(
                    "Delivery order not found: " + orderId.trim() + " on schedule "
                            + ScheduleIdSupport.normalize(schedule.getScheduleId()));
        }
        SubmissionHeaderSnapshot header = transactionSqlClient
                .findDeliverySubmissionHeader(schedule.getScheduleId(), shop.getDeliveryId())
                .orElse(null);
        List<StoredWeighmentDetailLine> stored = weighmentDetailSqlClient.listDeliveryCompleteLines(
                WeighmentSubmitKeys.deliveryTransactionKey(schedule.getScheduleId(), shop.getDeliveryId()));
        return buildDc(
                session, schedule, saleType, WeighmentDcResponse.TYPE_DELIVERY, shop, shop.getDeliveryId(), header, stored);
    }

    private WeighmentDcResponse buildDc(
            AuthenticatedSupervisor session,
            LiftingSchedule schedule,
            String saleType,
            String documentType,
            ScheduleDeliveryDto shop,
            String deliveryId,
            SubmissionHeaderSnapshot header,
            List<StoredWeighmentDetailLine> stored) {
        ScheduleBranchDto branch = ScheduleMapper.toBranch(schedule, session.getSupervisor());
        ScheduleCustomerDto customer = ScheduleMapper.toCustomer(schedule);
        var branchAddress = scheduleDcAddressSqlClient.findBranchAddress(
                schedule.getBranchId(), schedule.getBranchCode());
        branchAddress.ifPresent(address -> ScheduleMapper.applyBranchAddress(branch, address));
        if (customer.getCode() != null) {
            scheduleDcAddressSqlClient
                    .findIndentCustomerAddress(customer.getCode(), customer.getLocation())
                    .ifPresent(address -> ScheduleMapper.applyCustomerAddress(customer, address));
        }

        ScheduleDcDocumentDto print = ScheduleMapper.toDcDocument(schedule);
        applyItemGst(print, schedule);
        applyRegisteredOffice(print, schedule, branchAddress.map(PartyAddress::regionCode).orElse(null));
        if (customer.getGstNo() != null) {
            print.setTraderGstNo(customer.getGstNo());
        }
        if (print.getPlaceOfSupply() == null) {
            print.setPlaceOfSupply(firstNonBlank(customer.getState(), branch.getState()));
        }

        List<WeighmentDcLineDto> lines = mapLines(stored, WeighmentDcResponse.TYPE_DELIVERY.equals(documentType));
        WeighmentDcTotalsDto totals = totalsFrom(header, lines);

        WeighmentDcResponse.WeighmentDcResponseBuilder builder = WeighmentDcResponse.builder()
                .success(true)
                .documentType(documentType)
                .scheduleId(ScheduleIdSupport.normalize(schedule.getScheduleId()))
                .scheduledAt(ScheduleMapper.formatScheduledAt(schedule.getScheduledAt()))
                .saleType(saleType)
                .birdType(headerBirdType(header, schedule))
                .scaleType(schedule.getScaleType())
                .branch(branch)
                .customer(customer)
                .supervisor(toSupervisor(session, schedule))
                .vehicle(toVehicle(schedule, header))
                .print(print)
                .lines(lines)
                .totals(totals)
                .weighmentStartedAt(formatTs(header == null ? null : header.wmStartTime()))
                .weighmentCompletedAt(formatTs(header == null ? null : header.wmEndTime()))
                .scaleNo(header == null ? null : blankToNull(header.scaleNo()))
                .birdStockNos(header == null ? null : header.birdStockNos())
                .feedStock(header == null ? null : header.feedStockBags());

        if (WeighmentDcResponse.TYPE_LIFTING.equals(documentType)) {
            PartyContactDto farmer = new PartyContactDto();
            farmer.setId(schedule.getFarmerId());
            farmer.setName(schedule.getFarmerName());
            farmer.setMobile(schedule.getFarmerMobile());
            builder.farmer(farmer);
            builder.farm(ScheduleMapper.toFarmSummary(schedule));
            builder.plannedQuantityKg(schedule.getPlannedQuantityKg());
        } else {
            builder.deliveryId(deliveryId);
            builder.deliveryCustomer(toDeliveryCustomer(shop));
            builder.orderedQuantityKg(
                    header != null && header.orderedQty() != null ? header.orderedQty() : shop.getQuantityKg());
        }
        return builder.build();
    }

    private void applyItemGst(ScheduleDcDocumentDto print, LiftingSchedule schedule) {
        String itemCode = DcItemCodeSupport.resolveSegment1(schedule, erpProperties);
        Long organizationId = DcItemCodeSupport.parseOrganizationId(schedule.getBranchId());
        String breed = firstNonBlank(schedule.getProductName(), schedule.getOperationType());
        itemGstRepository
                .find(itemCode, organizationId, schedule.getBranchCode(), breed)
                .ifPresent(info -> {
                    if (info.hsnCode() != null) {
                        print.setHsnCode(info.hsnCode());
                    }
                    if (info.taxCode() != null) {
                        print.setTaxCode(info.taxCode());
                    }
                });
    }

    private void applyRegisteredOffice(ScheduleDcDocumentDto print, LiftingSchedule schedule, String regionCode) {
        scheduleDcAddressSqlClient
                .findRegisteredOffice(schedule.getBranchId(), schedule.getBranchCode())
                .map(RegisteredOfficeAddressSupport::parse)
                .ifPresent(office -> ScheduleMapper.applyRegisteredOffice(print, office));
        if (regionCode != null && !regionCode.isBlank()) {
            ScheduleMapper.applyDcRegionCompany(print, regionCode, print.getCompany());
        }
    }

    private static ScheduleCustomerDto toDeliveryCustomer(ScheduleDeliveryDto shop) {
        if (shop == null) {
            return null;
        }
        return ScheduleCustomerDto.builder()
                .code(blankToNull(shop.getCustomerCode()))
                .name(firstNonBlank(shop.getCustomerName(), shop.getDropPointName()))
                .location(blankToNull(shop.getLocation()))
                .address(blankToNull(shop.getLocation()))
                .gstNo(blankToNull(shop.getGstNo()))
                .build();
    }

    private static ScheduleSupervisorDto toSupervisor(AuthenticatedSupervisor session, LiftingSchedule schedule) {
        ScheduleSupervisorDto dto = new ScheduleSupervisorDto();
        String liftingCode = blankToNull(schedule.getSupervisorId());
        dto.setCode(liftingCode != null ? liftingCode : session.getSupervisor().getCode());
        if (liftingCode == null
                || liftingCode.equalsIgnoreCase(session.getSupervisor().getCode())) {
            dto.setName(session.getSupervisor().getName());
        }
        return dto;
    }

    private static ScheduleVehicleDto toVehicle(LiftingSchedule schedule, SubmissionHeaderSnapshot header) {
        ScheduleVehicleDto vehicle = new ScheduleVehicleDto();
        String number = header == null ? null : blankToNull(header.vehicleNo());
        vehicle.setNumber(number != null ? number : schedule.getVehicleNumber());
        vehicle.setDriverName(schedule.getDriverName());
        vehicle.setDriverMobile(schedule.getDriverMobile());
        return vehicle;
    }

    private static List<WeighmentDcLineDto> mapLines(List<StoredWeighmentDetailLine> stored, boolean includeCategory) {
        List<WeighmentDcLineDto> lines = new ArrayList<>();
        if (stored == null) {
            return lines;
        }
        int fallbackSeq = 1;
        for (StoredWeighmentDetailLine row : stored) {
            int seq = row.lineSequence() != null ? row.lineSequence() : fallbackSeq;
            WeighmentDcLineDto.WeighmentDcLineDtoBuilder line = WeighmentDcLineDto.builder()
                    .sequence(seq)
                    .birdsApplied(row.birdsApplied())
                    .net(row.netWt())
                    .averageBirdWeight(row.avgBirdWt())
                    .weightDifference(row.weightDifference());
            if (includeCategory) {
                line.category(displayCategory(row.category()));
            }
            lines.add(line.build());
            fallbackSeq++;
        }
        return lines;
    }

    private static WeighmentDcTotalsDto totalsFrom(
            SubmissionHeaderSnapshot header, List<WeighmentDcLineDto> lines) {
        int birds = 0;
        double net = 0D;
        boolean anyNet = false;
        for (WeighmentDcLineDto line : lines) {
            if (line.getBirdsApplied() != null) {
                birds += line.getBirdsApplied();
            }
            if (line.getNet() != null) {
                net += line.getNet();
                anyNet = true;
            }
        }
        Integer totalBirds = header != null && header.noOfBirds() != null ? header.noOfBirds() : birds;
        Double netWeight = header != null && header.netWt() != null ? header.netWt() : (anyNet ? net : null);
        Double avg = header != null ? header.avgWt() : null;
        if (avg == null && totalBirds != null && totalBirds > 0 && netWeight != null) {
            avg = netWeight / totalBirds;
        }
        return WeighmentDcTotalsDto.builder()
                .totalBirds(totalBirds)
                .netWeightKg(netWeight)
                .averageBirdWeightKg(avg)
                .build();
    }

    private static String headerBirdType(SubmissionHeaderSnapshot header, LiftingSchedule schedule) {
        if (header != null && header.birdType() != null && !header.birdType().isBlank()) {
            return header.birdType().trim();
        }
        return BirdTypeNormalizer.fromOrderType(schedule.getOperationType());
    }

    private static String liftingSupervisorCode(AuthenticatedSupervisor session, LiftingSchedule schedule) {
        if (schedule.getSupervisorId() != null && !schedule.getSupervisorId().isBlank()) {
            return schedule.getSupervisorId().trim();
        }
        return session.getSupervisor().getCode();
    }

    private static String displayCategory(String category) {
        if (category == null || category.isBlank()) {
            return "Normal";
        }
        String raw = category.trim();
        return raw.substring(0, 1).toUpperCase(Locale.ROOT)
                + raw.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String formatTs(Timestamp timestamp) {
        return timestamp == null ? null : ScheduleMapper.formatOptionalInstant(timestamp.toInstant());
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
