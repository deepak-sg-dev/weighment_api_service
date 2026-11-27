package com.suguna.weighment_api_service.admin.services.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.suguna.weighment_api_service.admin.dtos.requests.RetailScheduleFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.RetailScheduleReportResponse;
import com.suguna.weighment_api_service.admin.entities.*;
import com.suguna.weighment_api_service.admin.repositories.RetailScheduleReport_1Repository;
import com.suguna.weighment_api_service.admin.services.RetailScheduleReport_1Service;
import com.suguna.weighment_api_service.admin.specifications.ReportScheduleSpecification_1;
import com.suguna.weighment_api_service.admin.utils.CommonPdfPageEvent;
import com.suguna.weighment_api_service.admin.utils.PdfTableUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetailScheduleReport_1ServiceImpl implements RetailScheduleReport_1Service {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter DOWNLOAD_TIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final int IN_LIMIT = 999;

    private final RetailScheduleReport_1Repository scheduleRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<RetailScheduleReportResponse> getRetailScheduleReport(RetailScheduleFilterRequest filter) {
        if (filter.getFromDate() != null && filter.getToDate() != null
                && filter.getFromDate().isAfter(filter.getToDate())) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }

        // 1. Fetch base schedule entities using indexed specification
        List<BirdsLiftingOrderEntity_1> schedules =
                scheduleRepo.findAll(ReportScheduleSpecification_1.withFilters(filter));

        if (schedules.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Branch lookup map
        Set<String> branchCodes = schedules.stream()
                .map(BirdsLiftingOrderEntity_1::getBranchCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());

        Map<String, String> branchMap = findByFieldIn(OrganizationMvEntity_1.class, "branchCode", branchCodes).stream()
                .collect(Collectors.toMap(
                        OrganizationMvEntity_1::getBranchCode,
                        OrganizationMvEntity_1::getBranchName,
                        (a, b) -> a));

        // 3. Supervisor lookup map
        Set<String> empCodes = schedules.stream()
                .map(BirdsLiftingOrderEntity_1::getMobileUserEmpId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toSet());

        Map<String, String> supervisorMap = findByFieldIn(DeviceDataEntity_1.class, "empCode", empCodes).stream()
                .collect(Collectors.toMap(
                        DeviceDataEntity_1::getEmpCode,
                        DeviceDataEntity_1::getEmpName,
                        (a, b) -> a));

        // 4. Retail orders lookup (starting from small SUG_MAI_RETAIL_ORDER_HDR)
        Map<String, List<RetailOrderDtlEntity_1>> retailOrderByIndentMap = loadRetailOrdersByIndent();

        // 5. Collect all distinct customer account numbers
        Set<String> accountNumbers = new HashSet<>();
        for (BirdsLiftingOrderEntity_1 s : schedules) {
            if (s.getCustomerCode() != null) {
                accountNumbers.add(String.valueOf(s.getCustomerCode()));
            }
            List<RetailOrderDtlEntity_1> dtls = retailOrderByIndentMap.get(s.getErpIndentId());
            if (dtls != null) {
                for (RetailOrderDtlEntity_1 d : dtls) {
                    if (StringUtils.hasText(d.getCustomerCode())) {
                        accountNumbers.add(d.getCustomerCode());
                    }
                }
            }
        }

        // 6. Customer name & primary address lookup maps
        List<CustomerAccountEntity_1> accounts = findByFieldIn(CustomerAccountEntity_1.class, "accountNumber", accountNumbers);

        Map<String, CustomerAccountEntity_1> accountByNumber = accounts.stream()
                .collect(Collectors.toMap(CustomerAccountEntity_1::getAccountNumber, Function.identity(), (a, b) -> a));

        Set<Long> partyIds = accounts.stream()
                .map(CustomerAccountEntity_1::getPartyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> partyNameMap = findByFieldIn(HzPartyEntity_1.class, "partyId", partyIds).stream()
                .collect(Collectors.toMap(HzPartyEntity_1::getPartyId, HzPartyEntity_1::getPartyName, (a, b) -> a));

        Map<Long, String> addressByCustAcctId = loadPrimaryAddresses(accounts);

        // 7. Assemble final report rows
        List<RetailScheduleReportResponse> responseList = new ArrayList<>();
        for (BirdsLiftingOrderEntity_1 s : schedules) {
            String branchName = branchMap.get(s.getBranchCode());
            String supervisor = s.getMobileUserEmpId() != null
                    ? supervisorMap.get(String.valueOf(s.getMobileUserEmpId()))
                    : null;
            String downloadLog = formatDownloadLog(s);
            String defaultCustCode = s.getCustomerCode() != null ? String.valueOf(s.getCustomerCode()) : null;

            List<RetailOrderDtlEntity_1> orders = retailOrderByIndentMap.get(s.getErpIndentId());
            if (orders == null || orders.isEmpty()) {
                CustomerAccountEntity_1 acct = defaultCustCode != null ? accountByNumber.get(defaultCustCode) : null;
                String custName = (acct != null && partyNameMap.get(acct.getPartyId()) != null)
                        ? partyNameMap.get(acct.getPartyId())
                        : s.getCusName();
                String custAddr = acct != null ? addressByCustAcctId.get(acct.getCustAccountId()) : null;

                responseList.add(RetailScheduleReportResponse.builder()
                        .scheduleDate(s.getCreationDate() != null ? s.getCreationDate().format(DATE_TIME_FMT) : null)
                        .branchName(branchName)
                        .supervisor(supervisor)
                        .farmName(s.getFarmerName())
                        .customerCode(defaultCustCode)
                        .customerName(custName)
                        .customerAddress(custAddr)
                        .weight(null)
                        .quantity(null)
                        .deliveryStatus(null)
                        .deliveryDate(null)
                        .downloadLog(downloadLog)
                        .erpSchCode(s.getErpIndentId())
                        .status(s.getLiftingStatus())
                        .build());
            } else {
                for (RetailOrderDtlEntity_1 d : orders) {
                    String effCustCode = StringUtils.hasText(d.getCustomerCode()) ? d.getCustomerCode() : defaultCustCode;
                    CustomerAccountEntity_1 acct = effCustCode != null ? accountByNumber.get(effCustCode) : null;
                    String custName = (acct != null && partyNameMap.get(acct.getPartyId()) != null)
                            ? partyNameMap.get(acct.getPartyId())
                            : s.getCusName();
                    String custAddr = acct != null ? addressByCustAcctId.get(acct.getCustAccountId()) : null;

                    responseList.add(RetailScheduleReportResponse.builder()
                            .scheduleDate(s.getCreationDate() != null ? s.getCreationDate().format(DATE_TIME_FMT) : null)
                            .branchName(branchName)
                            .supervisor(supervisor)
                            .farmName(s.getFarmerName())
                            .customerCode(effCustCode)
                            .customerName(custName)
                            .customerAddress(custAddr)
                            .weight(d.getWeight())
                            .quantity(d.getQuantity())
                            .deliveryStatus(d.getDelvStatus())
                            .deliveryDate(d.getDelvEndDt() != null ? d.getDelvEndDt().format(DATE_TIME_FMT) : null)
                            .downloadLog(downloadLog)
                            .erpSchCode(s.getErpIndentId())
                            .status(s.getLiftingStatus())
                            .build());
                }
            }
        }

        return responseList;
    }

    @Override
    public ByteArrayInputStream exportRetailScheduleToExcel(
            List<RetailScheduleReportResponse> list,
            RetailScheduleFilterRequest filter) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Retail Schedule Report");
            int rowIdx = 0;

            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            Row titleRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("RETAIL SCHEDULE REPORT");
            titleCell.setCellStyle(boldStyle);

            if (filter != null && filter.getFromDate() != null && filter.getToDate() != null) {
                Row periodRow = sheet.createRow(rowIdx++);
                periodRow.createCell(0).setCellValue(
                        "Period: " + filter.getFromDate().format(DATE_FMT)
                                + " to " + filter.getToDate().format(DATE_FMT)
                );
            }

            rowIdx++;

            BigDecimal totalWeight = list.stream()
                    .map(RetailScheduleReportResponse::getWeight)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalQuantity = list.stream()
                    .map(RetailScheduleReportResponse::getQuantity)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Row summaryHeader = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell sumHdrCell = summaryHeader.createCell(0);
            sumHdrCell.setCellValue("Summary");
            sumHdrCell.setCellStyle(boldStyle);

            Row summaryRow = sheet.createRow(rowIdx++);
            summaryRow.createCell(0).setCellValue("Total Records");
            summaryRow.createCell(1).setCellValue(list.size());
            summaryRow.createCell(2).setCellValue("Total Weight");
            summaryRow.createCell(3).setCellValue(totalWeight.doubleValue());
            summaryRow.createCell(4).setCellValue("Total Quantity");
            summaryRow.createCell(5).setCellValue(totalQuantity.doubleValue());

            rowIdx++;

            String[] headers = {
                    "Schedule Date", "Branch Name", "Supervisor", "Farm Name",
                    "Customer Code", "Customer Name", "Customer Address",
                    "Weight", "Quantity", "Delivery Status", "Delivery Date",
                    "Download Log", "ERP Sch Code", "Status"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            for (RetailScheduleReportResponse d : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getScheduleDate() != null ? d.getScheduleDate() : "");
                row.createCell(1).setCellValue(d.getBranchName() != null ? d.getBranchName() : "");
                row.createCell(2).setCellValue(d.getSupervisor() != null ? d.getSupervisor() : "");
                row.createCell(3).setCellValue(d.getFarmName() != null ? d.getFarmName() : "");
                row.createCell(4).setCellValue(d.getCustomerCode() != null ? d.getCustomerCode() : "");
                row.createCell(5).setCellValue(d.getCustomerName() != null ? d.getCustomerName() : "");
                row.createCell(6).setCellValue(d.getCustomerAddress() != null ? d.getCustomerAddress() : "");
                row.createCell(7).setCellValue(d.getWeight() != null ? d.getWeight().doubleValue() : 0.0);
                row.createCell(8).setCellValue(d.getQuantity() != null ? d.getQuantity().doubleValue() : 0.0);
                row.createCell(9).setCellValue(d.getDeliveryStatus() != null ? d.getDeliveryStatus() : "");
                row.createCell(10).setCellValue(d.getDeliveryDate() != null ? d.getDeliveryDate() : "");
                row.createCell(11).setCellValue(d.getDownloadLog() != null ? d.getDownloadLog() : "");
                row.createCell(12).setCellValue(d.getErpSchCode() != null ? d.getErpSchCode() : "");
                row.createCell(13).setCellValue(d.getStatus() != null ? d.getStatus() : "");
            }

            Row totalRow = sheet.createRow(rowIdx);
            org.apache.poi.ss.usermodel.Cell totalLbl = totalRow.createCell(6);
            totalLbl.setCellValue("TOTAL");
            totalLbl.setCellStyle(boldStyle);

            org.apache.poi.ss.usermodel.Cell totalWtCell = totalRow.createCell(7);
            totalWtCell.setCellValue(totalWeight.doubleValue());
            totalWtCell.setCellStyle(boldStyle);

            org.apache.poi.ss.usermodel.Cell totalQtyCell = totalRow.createCell(8);
            totalQtyCell.setCellValue(totalQuantity.doubleValue());
            totalQtyCell.setCellStyle(boldStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Error while generating Retail Schedule Excel Report", e);
            throw new RuntimeException("Error while generating Retail Schedule Excel Report", e);
        }
    }

    @Override
    public ByteArrayInputStream exportRetailScheduleToPdf(
            List<RetailScheduleReportResponse> list,
            RetailScheduleFilterRequest filter) {

        Document document = new Document(PageSize.A4.rotate(), 10, 10, 12, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            String subtitle = "";
            if (filter != null && filter.getFromDate() != null && filter.getToDate() != null) {
                subtitle = "Period: " + filter.getFromDate().format(DATE_FMT) + " to " + filter.getToDate().format(DATE_FMT);
            }

            writer.setPageEvent(new CommonPdfPageEvent("RETAIL SCHEDULE REPORT", subtitle));
            document.open();

            BigDecimal totalWeight = list.stream()
                    .map(RetailScheduleReportResponse::getWeight)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalQuantity = list.stream()
                    .map(RetailScheduleReportResponse::getQuantity)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPTable table = new PdfPTable(14);
            table.setWidthPercentage(100);
            table.setHeaderRows(1);
            table.setSplitRows(false);
            table.setWidths(new float[]{2.3f, 2.2f, 2.8f, 2.5f, 1.8f, 3.2f, 3.8f, 1.5f, 1.5f, 1.8f, 2.1f, 2.6f, 2.0f, 1.6f});

            table.addCell(PdfTableUtil.headerCellLeft("Schedule Date"));
            table.addCell(PdfTableUtil.headerCellLeft("Branch Name"));
            table.addCell(PdfTableUtil.headerCellLeft("Supervisor"));
            table.addCell(PdfTableUtil.headerCellLeft("Farm Name"));
            table.addCell(PdfTableUtil.headerCellLeft("Cust Code"));
            table.addCell(PdfTableUtil.headerCellLeft("Customer Name"));
            table.addCell(PdfTableUtil.headerCellLeft("Customer Address"));
            table.addCell(PdfTableUtil.headerCellRight("Weight"));
            table.addCell(PdfTableUtil.headerCellRight("Quantity"));
            table.addCell(PdfTableUtil.headerCellLeft("Delivery Status"));
            table.addCell(PdfTableUtil.headerCellLeft("Delivery Date"));
            table.addCell(PdfTableUtil.headerCellLeft("Download Log"));
            table.addCell(PdfTableUtil.headerCellLeft("ERP Sch Code"));
            table.addCell(PdfTableUtil.headerCellLeft("Status"));

            int index = 0;
            for (RetailScheduleReportResponse d : list) {
                Color bg = (index % 2 == 0) ? new Color(245, 245, 245) : Color.WHITE;

                table.addCell(PdfTableUtil.dataCell(d.getScheduleDate(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getBranchName(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getSupervisor(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getFarmName(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getCustomerCode(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getCustomerName(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getCustomerAddress(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getWeight(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getQuantity(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getDeliveryStatus(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getDeliveryDate(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getDownloadLog(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getErpSchCode(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getStatus(), bg));

                index++;
            }

            Color totalBg = new Color(220, 230, 241);

            PdfPCell totalLabel = PdfTableUtil.dataCellBold("TOTAL", totalBg, Element.ALIGN_RIGHT);
            totalLabel.setColspan(7);
            table.addCell(totalLabel);

            table.addCell(PdfTableUtil.dataCellBold(totalWeight, totalBg, Element.ALIGN_RIGHT));
            table.addCell(PdfTableUtil.dataCellBold(totalQuantity, totalBg, Element.ALIGN_RIGHT));

            PdfPCell remainingSpacer = PdfTableUtil.dataCell("", totalBg);
            remainingSpacer.setColspan(5);
            table.addCell(remainingSpacer);

            document.add(table);
            document.close();

        } catch (Exception e) {
            log.error("Error while generating Retail Schedule PDF Report", e);
            throw new RuntimeException("Error while generating Retail Schedule PDF Report", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // ==================== JPA CRITERIA LOOKUP HELPERS ====================

    private <T, K> List<T> findByFieldIn(Class<T> entityClass, String fieldName, Collection<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        List<K> keyList = new ArrayList<>(keys);
        List<T> results = new ArrayList<>();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        for (int i = 0; i < keyList.size(); i += IN_LIMIT) {
            List<K> batch = keyList.subList(i, Math.min(i + IN_LIMIT, keyList.size()));
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root).where(root.get(fieldName).in(batch));
            results.addAll(entityManager.createQuery(cq).getResultList());
        }
        return results;
    }

    private Map<String, List<RetailOrderDtlEntity_1>> loadRetailOrdersByIndent() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RetailOrderHdrEntity_1> hdrQuery = cb.createQuery(RetailOrderHdrEntity_1.class);
        hdrQuery.select(hdrQuery.from(RetailOrderHdrEntity_1.class));
        List<RetailOrderHdrEntity_1> hdrs = entityManager.createQuery(hdrQuery).getResultList();

        if (hdrs.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> reqHeaderIds = hdrs.stream()
                .map(RetailOrderHdrEntity_1::getReqHeaderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Long> retailHdrIdByReqHdrId = hdrs.stream()
                .filter(h -> h.getReqHeaderId() != null)
                .collect(Collectors.toMap(RetailOrderHdrEntity_1::getReqHeaderId, RetailOrderHdrEntity_1::getHeaderId, (a, b) -> a));

        List<OeOrderHeaderEntity_1> oeHeaders = findByFieldIn(OeOrderHeaderEntity_1.class, "sourceDocumentId", reqHeaderIds);
        if (oeHeaders.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Long> retailHdrIdByOeHdrId = new HashMap<>();
        for (OeOrderHeaderEntity_1 oh : oeHeaders) {
            Long retailHdrId = retailHdrIdByReqHdrId.get(oh.getSourceDocumentId());
            if (retailHdrId != null) {
                retailHdrIdByOeHdrId.put(oh.getHeaderId(), retailHdrId);
            }
        }

        List<OeOrderLineEntity_1> oeLines = findByFieldIn(OeOrderLineEntity_1.class, "headerId", retailHdrIdByOeHdrId.keySet());
        List<RetailOrderDtlEntity_1> dtls = findByFieldIn(RetailOrderDtlEntity_1.class, "headerId", new HashSet<>(retailHdrIdByOeHdrId.values()));

        Map<Long, List<RetailOrderDtlEntity_1>> dtlsByRetailHdrId = dtls.stream()
                .collect(Collectors.groupingBy(RetailOrderDtlEntity_1::getHeaderId));

        Map<String, List<RetailOrderDtlEntity_1>> result = new HashMap<>();
        for (OeOrderLineEntity_1 line : oeLines) {
            if (StringUtils.hasText(line.getAttribute2())) {
                Long retailHdrId = retailHdrIdByOeHdrId.get(line.getHeaderId());
                List<RetailOrderDtlEntity_1> matchedDtls = dtlsByRetailHdrId.get(retailHdrId);
                if (matchedDtls != null) {
                    result.computeIfAbsent(line.getAttribute2(), k -> new ArrayList<>()).addAll(matchedDtls);
                }
            }
        }
        return result;
    }

    private Map<Long, String> loadPrimaryAddresses(List<CustomerAccountEntity_1> accounts) {
        if (accounts.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> custAcctIds = accounts.stream()
                .map(CustomerAccountEntity_1::getCustAccountId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<HzCustAcctSiteEntity_1> sites = findByFieldIn(HzCustAcctSiteEntity_1.class, "custAccountId", custAcctIds).stream()
                .filter(s -> "A".equalsIgnoreCase(s.getStatus()))
                .toList();

        Set<Long> partySiteIds = sites.stream()
                .map(HzCustAcctSiteEntity_1::getPartySiteId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Long> locationIdByPartySiteId = findByFieldIn(HzPartySiteEntity_1.class, "partySiteId", partySiteIds).stream()
                .filter(ps -> "Y".equalsIgnoreCase(ps.getIdentifyingAddressFlag()) && ps.getLocationId() != null)
                .collect(Collectors.toMap(HzPartySiteEntity_1::getPartySiteId, HzPartySiteEntity_1::getLocationId, (a, b) -> a));

        Map<Long, HzLocationEntity_1> locationById = findByFieldIn(HzLocationEntity_1.class, "locationId", new HashSet<>(locationIdByPartySiteId.values())).stream()
                .collect(Collectors.toMap(HzLocationEntity_1::getLocationId, Function.identity(), (a, b) -> a));

        Map<Long, String> addressByCustAcctId = new HashMap<>();
        for (HzCustAcctSiteEntity_1 site : sites) {
            if (addressByCustAcctId.containsKey(site.getCustAccountId())) {
                continue;
            }
            Long locId = locationIdByPartySiteId.get(site.getPartySiteId());
            HzLocationEntity_1 loc = locId != null ? locationById.get(locId) : null;
            if (loc != null) {
                String formatted = formatAddress(loc);
                if (StringUtils.hasText(formatted)) {
                    addressByCustAcctId.put(site.getCustAccountId(), formatted);
                }
            }
        }
        return addressByCustAcctId;
    }

    private String formatAddress(HzLocationEntity_1 loc) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(loc.getAddress1())) {
            sb.append(loc.getAddress1().trim());
        }
        if (StringUtils.hasText(loc.getAddress2())) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(loc.getAddress2().trim());
        }
        if (StringUtils.hasText(loc.getCity())) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(loc.getCity().trim());
        }
        return sb.toString().trim();
    }

    private String formatDownloadLog(BirdsLiftingOrderEntity_1 schedule) {
        if ("Y".equalsIgnoreCase(schedule.getDownloadFlag())) {
            if (schedule.getDownloadedDate() != null) {
                return "Downloaded on " + schedule.getDownloadedDate().format(DOWNLOAD_TIME_FMT);
            }
            return "Downloaded";
        }
        return "Not Downloaded";
    }
}
