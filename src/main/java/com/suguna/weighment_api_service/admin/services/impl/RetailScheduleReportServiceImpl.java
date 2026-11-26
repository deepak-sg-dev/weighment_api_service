package com.suguna.weighment_api_service.admin.services.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.suguna.weighment_api_service.admin.Projection.RetailScheduleReportProjection;
import com.suguna.weighment_api_service.admin.dtos.requests.RetailScheduleFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.RetailScheduleReportResponse;
import com.suguna.weighment_api_service.admin.entities.BirdsLiftingOrderEntity;
import com.suguna.weighment_api_service.admin.repositories.RetailScheduleReportRepository;
import com.suguna.weighment_api_service.admin.services.RetailScheduleReportService;
import com.suguna.weighment_api_service.admin.specifications.ReportScheduleSpecification;
import com.suguna.weighment_api_service.admin.utils.CommonPdfPageEvent;
import com.suguna.weighment_api_service.admin.utils.PdfTableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetailScheduleReportServiceImpl implements RetailScheduleReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
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
                continue;
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

            // Title Row
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

            // Summary Calculations
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

            // Table Header Row
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

            // Data Rows
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

            // Totals Row
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

            // Table Headers
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

            // Data Rows
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

            // Total Row
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
