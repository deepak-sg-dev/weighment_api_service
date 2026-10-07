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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
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
             WHERE TO_CHAR(o.erpindentid) = TRIM(?)
               AND NVL(o.source, 'X') = ?
               AND NVL(o.application, ?) = ?
               AND """
            + ORDER_SUPERVISOR_MATCH;

    private static final String LIST_NTAIL_ORDERS_SQL =
            """
            SELECT o.refid,
                   o.order_no,
                   TO_CHAR(o.customer_code) AS customer_code,
                   NVL(o.quantity, 0) AS quantity,
                   NVL(o.weight, 0) AS weight,
                   o.item_name,
                   o.uploaded_flag,
                   o.message,
                   o.branch_code,
                   o.region,
                   c.customer_name,
                   c.mobile_no,
                   c.address
              FROM sug_mai_ntail_order o
              LEFT JOIN sug_mai_ntail_customer_mst c
                ON c.customer_number = TO_CHAR(o.customer_code)
               AND c.branch_code = o.branch_code
             WHERE o.branch_code = ?
               AND o.region = ?
               AND TRUNC(o.trans_date) = TRUNC(?)
             ORDER BY o.order_no DESC
            """;

    private static final String LOAD_NTAIL_BY_ORDER_SQL =
            """
            SELECT o.refid,
                   o.order_no,
                   TO_CHAR(o.customer_code) AS customer_code,
                   NVL(o.quantity, 0) AS quantity,
                   NVL(o.weight, 0) AS weight,
                   o.item_name,
                   o.uploaded_flag,
                   o.message,
                   o.branch_code,
                   o.region,
                   c.customer_name,
                   c.mobile_no,
                   c.address
              FROM sug_mai_ntail_order o
              LEFT JOIN sug_mai_ntail_customer_mst c
                ON c.customer_number = TO_CHAR(o.customer_code)
               AND c.branch_code = o.branch_code
             WHERE TO_CHAR(o.order_no) = TRIM(?)
               AND o.branch_code = ?
               AND o.region = ?
               AND TRUNC(o.trans_date) = TRUNC(?)
            """;

    private static final String MARK_ARRIVED_SQL =
            """
            UPDATE sug_mai_ntail_order o
               SET o.message = ?,
                   o.uploaded_date = SYSDATE
             WHERE TO_CHAR(o.order_no) = TRIM(?)
               AND TO_CHAR(o.customer_code) = TRIM(?)
               AND o.branch_code = ?
               AND o.region = ?
               AND TRUNC(o.trans_date) = TRUNC(?)
            """;

    private static final String COMPLETE_DELIVERY_SQL =
            """
            UPDATE sug_mai_ntail_order o
               SET o.uploaded_flag = 'Y',
                   o.message = ?,
                   o.uploaded_date = SYSDATE,
                   o.weight = NVL(?, o.weight),
                   o.quantity = NVL(?, o.quantity)
             WHERE TO_CHAR(o.order_no) = TRIM(?)
               AND TO_CHAR(o.customer_code) = TRIM(?)
               AND o.branch_code = ?
               AND o.region = ?
               AND TRUNC(o.trans_date) = TRUNC(?)
            """;

    private static final String INVOICE_STATUS_SQL =
            """
            SELECT s.local_dc_no,
                   s.local_receipt_no,
                   s.created_date
              FROM sug_mai_ntail_sales_data s
             WHERE s.orderdbid = ?
             ORDER BY s.created_date DESC
            """;

    private final JdbcTemplate jdbcTemplate;
    private final OracleErpProperties erpProperties;

    public RetailDeliverySqlClient(JdbcTemplate jdbcTemplate, OracleErpProperties erpProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.erpProperties = erpProperties;
    }

    public RetailScheduleContext loadRetailSchedule(String supervisorCode, String scheduleId) {
        try {
            return jdbcTemplate.queryForObject(
                    LOAD_RETAIL_SCHEDULE_SQL,
                    (rs, rowNum) -> mapScheduleContext(rs),
                    scheduleId,
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
        Timestamp orderDate = Timestamp.from(schedule.getOrderDate());
        return jdbcTemplate.query(
                LIST_NTAIL_ORDERS_SQL,
                (rs, rowNum) -> mapSummary(rs, schedule.getScheduleId()),
                schedule.getRetailBranchCode(),
                schedule.getRegionCode(),
                orderDate);
    }

    public RetailDeliveryOrderDetailDto getDeliveryOrder(RetailScheduleContext schedule, String retailOrderId) {
        Timestamp orderDate = Timestamp.from(schedule.getOrderDate());
        try {
            return jdbcTemplate.queryForObject(
                    LOAD_NTAIL_BY_ORDER_SQL,
                    (rs, rowNum) -> mapDetail(rs, schedule.getScheduleId()),
                    retailOrderId,
                    schedule.getRetailBranchCode(),
                    schedule.getRegionCode(),
                    orderDate);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Retail delivery order", retailOrderId);
        }
    }

    public List<RetailLocationDto> listRetailerLocations(RetailScheduleContext schedule) {
        Timestamp orderDate = Timestamp.from(schedule.getOrderDate());
        return jdbcTemplate.query(
                LIST_NTAIL_ORDERS_SQL,
                (rs, rowNum) -> {
                    RetailOrderRow row = mapRow(rs);
                    return RetailLocationDto.builder()
                            .retailOrderId(row.orderNo())
                            .retailerId(row.customerCode())
                            .retailerName(row.customerName())
                            .latitude(0)
                            .longitude(0)
                            .address(row.address())
                            .build();
                },
                schedule.getRetailBranchCode(),
                schedule.getRegionCode(),
                orderDate);
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
                MARK_ARRIVED_SQL,
                message,
                retailOrderId,
                request.getRetailerId(),
                schedule.getRetailBranchCode(),
                schedule.getRegionCode(),
                Timestamp.from(schedule.getOrderDate()));
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
            return CompleteRetailDeliveryResponse.builder()
                    .success(true)
                    .status("COMPLETED")
                    .deliveryReference(deliveryReference(retailOrderId))
                    .invoiceStatus("PENDING")
                    .build();
        }

        String message = formatCompletionMessage(request);
        int updated = jdbcTemplate.update(
                COMPLETE_DELIVERY_SQL,
                message,
                request.getDeliveredWeight(),
                request.getDeliveredBirdCount(),
                retailOrderId,
                request.getRetailerId(),
                schedule.getRetailBranchCode(),
                schedule.getRegionCode(),
                Timestamp.from(schedule.getOrderDate()));
        if (updated == 0) {
            throw new ResourceNotFoundException("Retail delivery order", retailOrderId);
        }
        return CompleteRetailDeliveryResponse.builder()
                .success(true)
                .status("COMPLETED")
                .deliveryReference(deliveryReference(retailOrderId))
                .invoiceStatus("PENDING")
                .build();
    }

    public Optional<String> findExpectedRetailerOtp(RetailScheduleContext schedule, String retailOrderId) {
        RetailOrderRow row = loadOrderRow(schedule, retailOrderId);
        return extractOtpFromMessage(row.message());
    }

    public CompleteRetailDeliveryResponse triggerInvoice(String retailOrderId) {
        return CompleteRetailDeliveryResponse.builder()
                .success(true)
                .status("PENDING")
                .deliveryReference(deliveryReference(retailOrderId))
                .invoiceStatus("PENDING")
                .build();
    }

    public RetailInvoiceStatusResponse getInvoiceStatus(String retailOrderId) {
        long orderNo = parseOrderNo(retailOrderId);
        List<RetailInvoiceStatusResponse> rows = jdbcTemplate.query(
                INVOICE_STATUS_SQL,
                (rs, rowNum) -> {
                    String receipt = rs.getString("local_receipt_no");
                    boolean generated = receipt != null && !receipt.isBlank();
                    return RetailInvoiceStatusResponse.builder()
                            .success(true)
                            .syncStatus(generated ? "SYNCED" : "PENDING")
                            .invoiceStatus(generated ? "GENERATED" : "PENDING")
                            .invoiceReference(
                                    generated ? "INV-" + receipt : null)
                            .build();
                },
                orderNo);
        if (rows.isEmpty()) {
            return RetailInvoiceStatusResponse.builder()
                    .success(true)
                    .syncStatus("PENDING")
                    .invoiceStatus("PENDING")
                    .build();
        }
        return rows.get(0);
    }

    private RetailOrderRow loadOrderRow(RetailScheduleContext schedule, String retailOrderId) {
        Timestamp orderDate = Timestamp.from(schedule.getOrderDate());
        try {
            return jdbcTemplate.queryForObject(
                    LOAD_NTAIL_BY_ORDER_SQL,
                    (rs, rowNum) -> mapRow(rs),
                    retailOrderId,
                    schedule.getRetailBranchCode(),
                    schedule.getRegionCode(),
                    orderDate);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResourceNotFoundException("Retail delivery order", retailOrderId);
        }
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
        RetailOrderRow row = mapRow(rs);
        return RetailDeliveryOrderSummaryDto.builder()
                .retailOrderId(row.orderNo())
                .retailerId(row.customerCode())
                .retailerName(row.customerName())
                .salesOrderNo(row.orderNo())
                .orderedQuantity(row.weight())
                .orderedBirdCount(row.quantity())
                .status(mapDeliveryStatus(row.uploadedFlag(), row.message()))
                .build();
    }

    private RetailDeliveryOrderDetailDto mapDetail(ResultSet rs, String scheduleId) throws SQLException {
        RetailOrderRow row = mapRow(rs);
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
                .status(mapDeliveryStatus(row.uploadedFlag(), row.message()))
                .branchCode(row.branchCode())
                .regionCode(row.regionCode())
                .build();
    }

    private static RetailOrderRow mapRow(ResultSet rs) throws SQLException {
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
                rs.getString("address"));
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

    static String mapDeliveryStatus(String uploadedFlag, String message) {
        if ("Y".equalsIgnoreCase(uploadedFlag)) {
            return "COMPLETED";
        }
        if (message != null && message.toUpperCase(Locale.ROOT).startsWith("ARRIVED")) {
            return "ARRIVED";
        }
        return "PENDING_DELIVERY";
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

    static Optional<String> extractOtpFromMessage(String message) {
        if (message == null) {
            return Optional.empty();
        }
        String upper = message.toUpperCase(Locale.ROOT);
        int idx = upper.indexOf("RETAIL_OTP:");
        if (idx < 0) {
            return Optional.empty();
        }
        String tail = message.substring(idx + "RETAIL_OTP:".length());
        int end = tail.indexOf('|');
        String otp = (end > 0 ? tail.substring(0, end) : tail).trim();
        return otp.isEmpty() ? Optional.empty() : Optional.of(otp);
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
            String address) {}
}
