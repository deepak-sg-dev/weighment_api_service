package com.suguna.weighment_api_service.integration.oracle;

import com.suguna.weighment_api_service.config.OracleErpProperties;
import com.suguna.weighment_api_service.domain.RetailScheduleContext;
import com.suguna.weighment_api_service.dto.retail.CompleteRetailDeliveryRequest;
import com.suguna.weighment_api_service.dto.retail.CompleteRetailDeliveryResponse;
import com.suguna.weighment_api_service.dto.retail.MarkRetailerArrivedRequest;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryActionResponse;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderDetailDto;
import com.suguna.weighment_api_service.dto.retail.RetailDeliveryOrderSummaryDto;
import com.suguna.weighment_api_service.dto.retail.RetailInvoiceStatusResponse;
import com.suguna.weighment_api_service.dto.retail.RetailLocationDto;
import com.suguna.weighment_api_service.exception.ResourceNotFoundException;
import com.suguna.weighment_api_service.util.ScheduleIdSupport;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class RetailDeliverySqlClient {

    private static final String ORDER_SUPERVISOR_MATCH =
            """
             (TRIM(o.mobileuserempid) = TRIM(?)
              OR LTRIM(TRIM(o.mobileuserempid), '0') = LTRIM(TRIM(?), '0'))
            """;

    private static final String LOAD_RETAIL_SCHEDULE_SQL =
            """
            SELECT TO_CHAR(o.erpindentid) AS schedule_id,
                   o.mobileuserempid AS supervisor_code,
                   o.regioncode AS region_code,
                   o.cus_location,
                   o.order_request_date,
                   o.ordernumber AS internal_po_number
              FROM sug_mai_birds_lifting_order o
             WHERE """
            + ScheduleIdSupport.LIFTING_ORDER_ERP_INDENT_MATCH
            + """
               AND NVL(o.source, 'X') = ?
               AND NVL(o.application, ?) = ?
               AND """
            + ORDER_SUPERVISOR_MATCH;

    private final JdbcTemplate jdbcTemplate;
    private final OracleErpProperties erpProperties;
    private final RetailOrderLineSqlSupport retailOrderSql;
    private final String listRetailOrdersSql;
    private final String loadRetailOrderByNoSql;
    private final String markArrivedSql;
    private final String completeDeliverySql;
    private final String invoiceStatusSql;

    public RetailDeliverySqlClient(
            JdbcTemplate jdbcTemplate,
            OracleErpProperties erpProperties,
            RetailOrderLineSqlSupport retailOrderSql) {
        this.jdbcTemplate = jdbcTemplate;
        this.erpProperties = erpProperties;
        this.retailOrderSql = retailOrderSql;
        this.listRetailOrdersSql = retailOrderSql.selectDetailCoreColumns()
                + retailOrderSql.selectDelvStatusColumn()
                + retailOrderSql.selectDelvStartedColumn()
                + retailOrderSql.selectRetailOtpColumns()
                + retailOrderSql.selectDetailGpsColumns()
                + retailOrderSql.fromScheduleToDetailJoin()
                + retailOrderSql.whereLiftingScheduleForRetail()
                + """
             ORDER BY rod.order_no DESC
            """;
        this.loadRetailOrderByNoSql = retailOrderSql.selectDetailCoreColumns()
                + retailOrderSql.selectDelvStatusColumn()
                + retailOrderSql.selectDelvStartedColumn()
                + retailOrderSql.selectRetailOtpColumns()
                + retailOrderSql.selectDetailGpsColumns()
                + retailOrderSql.fromScheduleToDetailJoin()
                + retailOrderSql.whereLiftingScheduleForRetail()
                + RetailOrderLineSqlSupport.andDetailOrderNo();
        this.markArrivedSql =
                retailOrderSql.buildDetailUpdatePrefix()
                        + """
                        rod.message = ?,
                           rod.uploaded_date = SYSDATE
                        """
                        + RetailOrderLineSqlSupport.updateWhereClause()
                        + RetailOrderLineSqlSupport.whereDetailOrderNo()
                        + retailOrderSql.andDetailCustomerCodeFilter().trim()
                        + " AND "
                        + retailOrderSql.existsDetailLinkedToSchedule();
        this.completeDeliverySql =
                retailOrderSql.buildDetailUpdatePrefix()
                        + """
                        rod.uploaded_flag = 'Y',
                           rod.message = ?,
                           rod.uploaded_date = SYSDATE,
                           rod.weight = NVL(?, rod.weight),
                           rod.quantity = NVL(?, rod.quantity)"""
                        + retailOrderSql.buildMarkRetailOtpVerifiedSetSuffix()
                        + "\n"
                        + RetailOrderLineSqlSupport.updateWhereClause()
                        + RetailOrderLineSqlSupport.whereDetailOrderNo()
                        + retailOrderSql.andDetailCustomerCodeFilter().trim()
                        + " AND "
                        + retailOrderSql.existsDetailLinkedToSchedule();
        this.invoiceStatusSql = """
            SELECT roh.local_dc_no,
                   roh.local_receipt_no,
                   roh.creation_date
            """
                + retailOrderSql.fromScheduleToDetailJoin()
                + retailOrderSql.whereLiftingScheduleForRetail()
                + RetailOrderLineSqlSupport.andDetailOrderNo()
                + """
             ORDER BY roh.creation_date DESC
            """;
    }

    static String sanitizeOracleColumnName(String raw, String fallback) {
        String candidate = raw == null || raw.isBlank() ? fallback : raw.trim().toUpperCase(Locale.ROOT);
        if (!candidate.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalStateException("Invalid Oracle column name for retail GPS: " + candidate);
        }
        return candidate;
    }

    public RetailScheduleContext loadRetailSchedule(String supervisorCode, String scheduleId) {
        String normalizedScheduleId = ScheduleIdSupport.normalize(scheduleId);
        try {
            return jdbcTemplate.queryForObject(
                    LOAD_RETAIL_SCHEDULE_SQL,
                    (rs, rowNum) -> mapScheduleContext(rs),
                    normalizedScheduleId,
                    normalizedScheduleId,
                    erpProperties.getRetailLiftingSource(),
                    erpProperties.getApplicationCode(),
                    erpProperties.getApplicationCode(),
                    supervisorCode,
                    supervisorCode);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException(
                    "Retail schedule not found with id: "
                            + scheduleId
                            + " for supervisor "
                            + supervisorCode
                            + ". Expect SOURCE="
                            + erpProperties.getRetailLiftingSource()
                            + " on sug_mai_birds_lifting_order.");
        }
    }

    public List<RetailDeliveryOrderSummaryDto> listDeliveryOrders(RetailScheduleContext schedule) {
        return jdbcTemplate.query(
                listRetailOrdersSql,
                (rs, rowNum) -> mapSummary(rs, schedule.getScheduleId()),
                scheduleBindArgs(schedule));
    }

    public RetailDeliveryOrderDetailDto getDeliveryOrder(RetailScheduleContext schedule, String retailOrderId) {
        try {
            Object[] args = scheduleBindArgsWithOrder(schedule, retailOrderId);
            return jdbcTemplate.queryForObject(
                    loadRetailOrderByNoSql, (rs, rowNum) -> mapDetail(rs, schedule.getScheduleId()), args);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Retail delivery order", retailOrderId);
        }
    }

    public List<RetailLocationDto> listRetailerLocations(RetailScheduleContext schedule) {
        return jdbcTemplate.query(
                listRetailOrdersSql,
                (rs, rowNum) -> {
                    RetailOrderRow row = this.mapRow(rs);
                    RetailLocationDto location = new RetailLocationDto();
                    location.setRetailOrderId(row.orderNo());
                    location.setRetailerId(row.customerCode());
                    location.setRetailerName(row.customerName());
                    location.setLatitude(row.latitude());
                    location.setLongitude(row.longitude());
                    location.setAddress(row.address());
                    return location;
                },
                scheduleBindArgs(schedule));
    }

    public RetailDeliveryActionResponse markArrived(
            RetailScheduleContext schedule, String retailOrderId, MarkRetailerArrivedRequest request) {
        ensureOrderMatchesRetailer(schedule, retailOrderId, request.getRetailerId());
        String message = "ARRIVED:"
                + request.getArrivedAt()
                + "|LAT:"
                + request.getLocation().getLatitude()
                + "|LON:"
                + request.getLocation().getLongitude();
        int updated = jdbcTemplate.update(
                markArrivedSql,
                mergeUpdateArgs(message, retailOrderId, request.getRetailerId(), schedule));
        if (updated == 0) {
            throw new ResourceNotFoundException("Retail delivery order", retailOrderId);
        }
        return RetailDeliveryActionResponse.builder()
                .success(true)
                .retailOrderId(retailOrderId)
                .status("ARRIVED")
                .message("Retailer arrival recorded")
                .build();
    }

    public CompleteRetailDeliveryResponse completeDelivery(
            RetailScheduleContext schedule, String retailOrderId, CompleteRetailDeliveryRequest request) {
        ensureOrderMatchesRetailer(schedule, retailOrderId, request.getRetailerId());
        RetailOrderRow row = loadOrderRow(schedule, retailOrderId);
        if (isIdempotencyReplay(row.message(), request.getIdempotencyKey())) {
            return completedDeliveryResponse(retailOrderId, "COMPLETED");
        }

        String message = formatCompletionMessage(request);
        Object[] args = mergeCompleteDeliveryArgs(message, request, retailOrderId, schedule);
        int updated = jdbcTemplate.update(completeDeliverySql, args);
        if (updated == 0) {
            throw new ResourceNotFoundException("Retail delivery order", retailOrderId);
        }
        return completedDeliveryResponse(retailOrderId, "COMPLETED");
    }

    public RetailRetailerOtpResolution resolveRetailerOtp(RetailScheduleContext schedule, String retailOrderId) {
        RetailOrderRow row = loadOrderRow(schedule, retailOrderId);
        return RetailRetailerOtpSupport.resolve(
                erpProperties.isRetailOrderOtpUseDtlColumn(),
                row.retailOtp(),
                row.retailOtpExpiresAt(),
                row.message(),
                Instant.now());
    }

    public CompleteRetailDeliveryResponse triggerInvoice(String retailOrderId) {
        return completedDeliveryResponse(retailOrderId, "PENDING");
    }

    public RetailInvoiceStatusResponse getInvoiceStatus(String retailOrderId) {
        return RetailInvoiceStatusResponse.builder()
                .success(true)
                .syncStatus("PENDING")
                .invoiceStatus("PENDING")
                .build();
    }

    public RetailInvoiceStatusResponse getInvoiceStatus(RetailScheduleContext schedule, String retailOrderId) {
        try {
            Object[] args = scheduleBindArgsWithOrder(schedule, retailOrderId);
            List<RetailInvoiceStatusResponse> rows = jdbcTemplate.query(
                    invoiceStatusSql,
                    (rs, rowNum) -> {
                        String receipt = rs.getString("local_receipt_no");
                        boolean generated = receipt != null && !receipt.isBlank();
                        return RetailInvoiceStatusResponse.builder()
                                .success(true)
                                .syncStatus(generated ? "SYNCED" : "PENDING")
                                .invoiceStatus(generated ? "GENERATED" : "PENDING")
                                .invoiceReference(generated ? "INV-" + receipt : null)
                                .build();
                    },
                    args);
            if (rows.isEmpty()) {
                return getInvoiceStatus(retailOrderId);
            }
            return rows.get(0);
        } catch (RuntimeException ex) {
            return getInvoiceStatus(retailOrderId);
        }
    }

    private RetailOrderRow loadOrderRow(RetailScheduleContext schedule, String retailOrderId) {
        try {
            return jdbcTemplate.queryForObject(
                    loadRetailOrderByNoSql,
                    (rs, rowNum) -> this.mapRow(rs),
                    scheduleBindArgsWithOrder(schedule, retailOrderId));
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Retail delivery order", retailOrderId);
        }
    }

    /** JDBC bind order for {@link RetailOrderLineSqlSupport#whereLiftingScheduleForRetail()}. */
    public Object[] scheduleBindArgs(RetailScheduleContext schedule) {
        return new Object[] {
            schedule.getScheduleId(),
            schedule.getScheduleId(),
            erpProperties.getRetailLiftingSource(),
            erpProperties.getApplicationCode(),
            erpProperties.getApplicationCode(),
            schedule.getSupervisorCode(),
            schedule.getSupervisorCode()
        };
    }

    private Object[] scheduleBindArgsWithOrder(RetailScheduleContext schedule, String retailOrderId) {
        Object[] base = scheduleBindArgs(schedule);
        Object[] args = new Object[base.length + 1];
        System.arraycopy(base, 0, args, 0, base.length);
        args[base.length] = retailOrderId;
        return args;
    }

    private Object[] mergeUpdateArgs(
            String message, String retailOrderId, String retailerId, RetailScheduleContext schedule) {
        if (erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            Object[] args = new Object[2 + 7];
            args[0] = message;
            args[1] = retailOrderId;
            System.arraycopy(scheduleBindArgs(schedule), 0, args, 2, 7);
            return args;
        }
        Object[] args = new Object[3 + 7];
        args[0] = message;
        args[1] = retailOrderId;
        args[2] = retailerId;
        System.arraycopy(scheduleBindArgs(schedule), 0, args, 3, 7);
        return args;
    }

    private Object[] mergeCompleteDeliveryArgs(
            String message,
            CompleteRetailDeliveryRequest request,
            String retailOrderId,
            RetailScheduleContext schedule) {
        if (erpProperties.isRetailCustomerFromHzSiteEnabled()) {
            Object[] args = new Object[4 + 7];
            args[0] = message;
            args[1] = request.getDeliveredWeight();
            args[2] = request.getDeliveredBirdCount();
            args[3] = retailOrderId;
            System.arraycopy(scheduleBindArgs(schedule), 0, args, 4, 7);
            return args;
        }
        Object[] args = new Object[5 + 7];
        args[0] = message;
        args[1] = request.getDeliveredWeight();
        args[2] = request.getDeliveredBirdCount();
        args[3] = retailOrderId;
        args[4] = request.getRetailerId();
        System.arraycopy(scheduleBindArgs(schedule), 0, args, 5, 7);
        return args;
    }

    private void ensureOrderMatchesRetailer(
            RetailScheduleContext schedule, String retailOrderId, String retailerId) {
        RetailOrderRow row = loadOrderRow(schedule, retailOrderId);
        if (!row.customerCode().equals(retailerId.trim())) {
            throw new ResourceNotFoundException(
                    "Retailer " + retailerId + " does not match retail order " + retailOrderId);
        }
    }

    private RetailScheduleContext mapScheduleContext(ResultSet rs) throws SQLException {
        Timestamp orderDate = rs.getTimestamp("order_request_date");
        if (orderDate == null) {
            orderDate = new Timestamp(System.currentTimeMillis());
        }
        return RetailScheduleContext.builder()
                .scheduleId(rs.getString("schedule_id"))
                .supervisorCode(rs.getString("supervisor_code"))
                .regionCode(rs.getString("region_code"))
                .retailBranchCode(resolveRetailBranchCode(rs.getString("cus_location")))
                .orderDate(orderDate.toInstant())
                .internalPoNumber(readLong(rs, "internal_po_number"))
                .build();
    }

    private RetailDeliveryOrderSummaryDto mapSummary(ResultSet rs, String scheduleId) throws SQLException {
        RetailOrderRow row = this.mapRow(rs);
        OrderDeliveryPresentation presentation = orderDeliveryPresentation(row);
        return RetailDeliveryOrderSummaryDto.builder()
                .retailOrderId(row.orderNo())
                .retailerId(row.customerCode())
                .retailerName(row.customerName())
                .salesOrderNo(row.orderNo())
                .orderedQuantity(row.weight())
                .orderedBirdCount(row.quantity())
                .deliveryStatus(presentation.deliveryStatus())
                .deliveryStartedAt(presentation.deliveryStartedAt())
                .status(presentation.deliveryStatus())
                .build();
    }

    /** Re-reads retail order lines for the schedule and returns PENDING / IN_PROGRESS / COMPLETED. */
    public String aggregateScheduleDeliveryStatus(RetailScheduleContext schedule) {
        List<RetailDeliveryOrderSummaryDto> orders = listDeliveryOrders(schedule);
        return RetailOrderDeliveryStatus.aggregateForLiftingOrder(
                orders.stream().map(RetailDeliveryOrderSummaryDto::getDeliveryStatus).toList());
    }

    private RetailDeliveryOrderDetailDto mapDetail(ResultSet rs, String scheduleId) throws SQLException {
        RetailOrderRow row = this.mapRow(rs);
        OrderDeliveryPresentation presentation = orderDeliveryPresentation(row);
        return RetailDeliveryOrderDetailDto.builder()
                .retailOrderId(row.orderNo())
                .scheduleId(scheduleId)
                .retailerId(row.customerCode())
                .retailerName(row.customerName())
                .retailerMobile(row.mobileNo())
                .retailerAddress(row.address())
                .salesOrderNo(row.orderNo())
                .itemName(row.itemName())
                .orderedQuantity(row.weight())
                .orderedBirdCount(row.quantity())
                .deliveryStatus(presentation.deliveryStatus())
                .deliveryStartedAt(presentation.deliveryStartedAt())
                .status(presentation.deliveryStatus())
                .branchCode(row.branchCode())
                .regionCode(row.regionCode())
                .build();
    }

    private OrderDeliveryPresentation orderDeliveryPresentation(RetailOrderRow row) {
        String deliveryStatus = RetailOrderDeliveryStatus.resolveForOrder(
                row.delvStatus(),
                erpProperties.isRetailOrderDeliveryStatusEnabled(),
                row.uploadedFlag(),
                row.message(),
                row.deliveryStartedAt());
        Instant started = row.deliveryStartedAt();
        if (started == null) {
            started = DeliveryOrderMessageSupport.parseDeliveryStartedAt(row.message());
        }
        return new OrderDeliveryPresentation(deliveryStatus, formatDeliveryStartedAt(started));
    }

    private static String formatDeliveryStartedAt(Instant started) {
        if (started == null) {
            return null;
        }
        return DateTimeFormatter.ISO_INSTANT.format(started.atOffset(ZoneOffset.UTC));
    }

    private record OrderDeliveryPresentation(String deliveryStatus, String deliveryStartedAt) {}

    private RetailOrderRow mapRow(ResultSet rs) throws SQLException {
        return new RetailOrderRow(
                rs.getString("order_no"),
                rs.getString("customer_code"),
                rs.getDouble("weight"),
                rs.getInt("quantity"),
                rs.getString("item_name"),
                rs.getString("uploaded_flag"),
                rs.getString("message"),
                rs.getString("branch_code"),
                rs.getString("region"),
                rs.getString("customer_name"),
                rs.getString("mobile_no"),
                rs.getString("address"),
                rs.getDouble("retailer_latitude"),
                rs.getDouble("retailer_longitude"),
                rs.getString("delv_status"),
                readDeliveryStartedInstant(rs),
                rs.getString("retail_otp"),
                readOtpExpiresInstant(rs));
    }

    private static Instant readOtpExpiresInstant(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("otp_expires_at");
        return ts == null ? null : ts.toInstant();
    }

    private static Instant readDeliveryStartedInstant(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("delivery_started_date");
        return ts == null ? null : ts.toInstant();
    }

    static String resolveRetailBranchCode(String cusLocation) {
        if (cusLocation == null || cusLocation.isBlank()) {
            return "";
        }
        String trimmed = cusLocation.trim();
        int colon = trimmed.indexOf(':');
        if (colon > 0) {
            return trimmed.substring(0, colon).trim();
        }
        return trimmed;
    }

    private static CompleteRetailDeliveryResponse completedDeliveryResponse(
            String retailOrderId, String status) {
        return CompleteRetailDeliveryResponse.builder()
                .success(true)
                .status(status)
                .deliveryReference(deliveryReference(retailOrderId))
                .invoiceStatus("PENDING")
                .build();
    }

    static String deliveryReference(String retailOrderId) {
        return "DEL-" + retailOrderId;
    }

    static String formatCompletionMessage(CompleteRetailDeliveryRequest request) {
        String shopRef = request.getShopImage() == null ? "" : request.getShopImage().getReference();
        return "IDEMPOTENCY:"
                + request.getIdempotencyKey()
                + "|COMPLETED:"
                + request.getCompletedAt()
                + "|OTP_VERIFIED|IMG:"
                + shopRef
                + "|WT:"
                + request.getDeliveredWeight()
                + "|BIRDS:"
                + request.getDeliveredBirdCount()
                + (request.getRemarks() == null || request.getRemarks().isBlank()
                        ? ""
                        : "|REMARKS:" + request.getRemarks());
    }

    static boolean isIdempotencyReplay(String message, String idempotencyKey) {
        return message != null && message.contains("IDEMPOTENCY:" + idempotencyKey + "|");
    }

    private static Long readLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static long parseOrderNo(String retailOrderId) {
        try {
            return Long.parseLong(retailOrderId.replaceAll("\\D", ""));
        } catch (RuntimeException ex) {
            return 0L;
        }
    }

    private record RetailOrderRow(
            String orderNo,
            String customerCode,
            double weight,
            int quantity,
            String itemName,
            String uploadedFlag,
            String message,
            String branchCode,
            String regionCode,
            String customerName,
            String mobileNo,
            String address,
            double latitude,
            double longitude,
            String delvStatus,
            Instant deliveryStartedAt,
            String retailOtp,
            Instant retailOtpExpiresAt) {}
}
