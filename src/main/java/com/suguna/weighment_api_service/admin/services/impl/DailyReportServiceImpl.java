package com.suguna.weighment_api_service.admin.services.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.suguna.weighment_api_service.admin.Projection.DailyReportProjection;
import com.suguna.weighment_api_service.admin.dtos.requests.DailyReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.DailyReportListResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.DailyReportResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.repositories.DailyReportRepository;
import com.suguna.weighment_api_service.admin.services.DailyReportService;
import com.suguna.weighment_api_service.admin.utils.CommonPdfPageEvent;
import com.suguna.weighment_api_service.admin.utils.PdfTableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportServiceImpl implements DailyReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final DailyReportRepository dailyReportRepository;

    @Override
    @Transactional(readOnly = true)
    public DailyReportListResponse getDailyReport(DailyReportFilterRequest filter) {
        LocalDate effectiveFrom = resolveFromDate(filter);
        LocalDate effectiveTo = resolveToDate(filter, effectiveFrom);
        validateDateRange(effectiveFrom, effectiveTo);

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
                .map(this::mapProjectionToResponse)
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

    @Override
    @Transactional(readOnly = true)
    public List<DailyReportResponse> getAllDailyReportForExport(DailyReportFilterRequest filter) {
        LocalDate effectiveFrom = resolveFromDate(filter);
        LocalDate effectiveTo = resolveToDate(filter, effectiveFrom);
        validateDateRange(effectiveFrom, effectiveTo);

        return dailyReportRepository.findAllDailyReport(
                        effectiveFrom,
                        effectiveTo,
                        filter.getBranchCode(),
                        filter.getFarmCode()
                )
                .stream()
                .map(this::mapProjectionToResponse)
                .toList();
    }

    @Override
    public ByteArrayInputStream exportDailyReportToExcel(
            List<DailyReportResponse> list,
            DailyReportFilterRequest filter) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Daily Procurement Report");
            int rowIdx = 0;

            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            // Title Row
            Row titleRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DAILY PROCUREMENT REPORT");
            titleCell.setCellStyle(boldStyle);

            LocalDate effectiveFrom = resolveFromDate(filter);
            LocalDate effectiveTo = resolveToDate(filter, effectiveFrom);
            Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue(
                    "Period: " + effectiveFrom.format(DATE_FMT) + " to " + effectiveTo.format(DATE_FMT)
            );

            rowIdx++;

            // Summary Calculations
            BigDecimal totalWeight = list.stream()
                    .map(DailyReportResponse::getWeightOfBirds)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalBirds = list.stream()
                    .map(DailyReportResponse::getNoOfBirds)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            BigDecimal overallAvgWeight = totalBirds > 0
                    ? totalWeight.divide(BigDecimal.valueOf(totalBirds), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal totalFeedStock = list.stream()
                    .map(DailyReportResponse::getFeedStockBag)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Row summaryHeader = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell sumHdrCell = summaryHeader.createCell(0);
            sumHdrCell.setCellValue("Summary");
            sumHdrCell.setCellStyle(boldStyle);

            Row summaryRow = sheet.createRow(rowIdx++);
            summaryRow.createCell(0).setCellValue("Total Weight (Kgs)");
            summaryRow.createCell(1).setCellValue(totalWeight.doubleValue());
            summaryRow.createCell(2).setCellValue("Total Birds");
            summaryRow.createCell(3).setCellValue(totalBirds);
            summaryRow.createCell(4).setCellValue("Overall Avg Weight");
            summaryRow.createCell(5).setCellValue(overallAvgWeight.doubleValue());
            summaryRow.createCell(6).setCellValue("Total Feed Stock Bags");
            summaryRow.createCell(7).setCellValue(totalFeedStock.doubleValue());

            rowIdx++;

            // Table Headers (26 Columns)
            String[] headers = {
                    "Sch Code", "Farmer Name", "Farm Code", "Customer Name", "Customer Code",
                    "Supervisor Code", "Supervisor Name", "DC Number", "Slip No",
                    "WM Start Date", "WM Start Time", "WM End Time", "Vehicle No",
                    "Weight of Birds", "No of Birds", "Bird Type", "Avg Weight",
                    "Error Count", "Abort Count", "Feed Stock Bag", "Vehicle In",
                    "Vehicle Out", "Branch Code", "Sch Date", "Delivery Start Time", "Delivery End Time"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            // Data Rows
            for (DailyReportResponse d : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getSchCode() != null ? d.getSchCode() : "");
                row.createCell(1).setCellValue(d.getFarmerName() != null ? d.getFarmerName() : "");
                row.createCell(2).setCellValue(d.getFarmCode() != null ? d.getFarmCode() : "");
                row.createCell(3).setCellValue(d.getCustomerName() != null ? d.getCustomerName() : "");
                row.createCell(4).setCellValue(d.getCustomerCode() != null ? d.getCustomerCode() : "");
                row.createCell(5).setCellValue(d.getSupervisorCode() != null ? d.getSupervisorCode() : "");
                row.createCell(6).setCellValue(d.getSupervisorName() != null ? d.getSupervisorName() : "");
                row.createCell(7).setCellValue(d.getDcNumber() != null ? d.getDcNumber() : "");
                row.createCell(8).setCellValue(d.getSlipNo() != null ? d.getSlipNo() : "");
                row.createCell(9).setCellValue(d.getWeighmentStartDate() != null ? d.getWeighmentStartDate() : "");
                row.createCell(10).setCellValue(d.getWeighmentStartTime() != null ? d.getWeighmentStartTime() : "");
                row.createCell(11).setCellValue(d.getWeighmentEndTime() != null ? d.getWeighmentEndTime() : "");
                row.createCell(12).setCellValue(d.getVehicleNo() != null ? d.getVehicleNo() : "");
                row.createCell(13).setCellValue(d.getWeightOfBirds() != null ? d.getWeightOfBirds().doubleValue() : 0.0);
                row.createCell(14).setCellValue(d.getNoOfBirds() != null ? d.getNoOfBirds() : 0);
                row.createCell(15).setCellValue(d.getBirdType() != null ? d.getBirdType() : "");
                row.createCell(16).setCellValue(d.getAvgWeight() != null ? d.getAvgWeight().doubleValue() : 0.0);
                row.createCell(17).setCellValue(d.getErrorCount() != null ? d.getErrorCount() : 0);
                row.createCell(18).setCellValue(d.getAbortCount() != null ? d.getAbortCount() : 0);
                row.createCell(19).setCellValue(d.getFeedStockBag() != null ? d.getFeedStockBag().doubleValue() : 0.0);
                row.createCell(20).setCellValue(d.getVehicleIn() != null ? d.getVehicleIn() : "");
                row.createCell(21).setCellValue(d.getVehicleOut() != null ? d.getVehicleOut() : "");
                row.createCell(22).setCellValue(d.getBranchCode() != null ? d.getBranchCode() : "");
                row.createCell(23).setCellValue(d.getSchDate() != null ? d.getSchDate() : "");
                row.createCell(24).setCellValue(d.getDeliveryStartTime() != null ? d.getDeliveryStartTime() : "");
                row.createCell(25).setCellValue(d.getDeliveryEndTime() != null ? d.getDeliveryEndTime() : "");
            }

            // Totals Row
            Row totalRow = sheet.createRow(rowIdx);
            org.apache.poi.ss.usermodel.Cell totalLbl = totalRow.createCell(12);
            totalLbl.setCellValue("TOTAL");
            totalLbl.setCellStyle(boldStyle);

            org.apache.poi.ss.usermodel.Cell totalWtCell = totalRow.createCell(13);
            totalWtCell.setCellValue(totalWeight.doubleValue());
            totalWtCell.setCellStyle(boldStyle);

            org.apache.poi.ss.usermodel.Cell totalBirdsCell = totalRow.createCell(14);
            totalBirdsCell.setCellValue(totalBirds);
            totalBirdsCell.setCellStyle(boldStyle);

            org.apache.poi.ss.usermodel.Cell avgWtCell = totalRow.createCell(16);
            avgWtCell.setCellValue(overallAvgWeight.doubleValue());
            avgWtCell.setCellStyle(boldStyle);

            org.apache.poi.ss.usermodel.Cell feedCell = totalRow.createCell(19);
            feedCell.setCellValue(totalFeedStock.doubleValue());
            feedCell.setCellStyle(boldStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Error while generating Daily Excel Report", e);
            throw new RuntimeException("Error while generating Daily Excel Report", e);
        }
    }

    @Override
    public ByteArrayInputStream exportDailyReportToPdf(
            List<DailyReportResponse> list,
            DailyReportFilterRequest filter) {

        // A3 Landscape fits all 26 columns cleanly without text clipping
        Document document = new Document(PageSize.A3.rotate(), 10, 10, 12, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            LocalDate effectiveFrom = resolveFromDate(filter);
            LocalDate effectiveTo = resolveToDate(filter, effectiveFrom);
            String subtitle = "Period: " + effectiveFrom.format(DATE_FMT) + " to " + effectiveTo.format(DATE_FMT);

            writer.setPageEvent(new CommonPdfPageEvent("DAILY PROCUREMENT REPORT", subtitle));
            document.open();

            BigDecimal totalWeight = list.stream()
                    .map(DailyReportResponse::getWeightOfBirds)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalBirds = list.stream()
                    .map(DailyReportResponse::getNoOfBirds)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            BigDecimal overallAvgWeight = totalBirds > 0
                    ? totalWeight.divide(BigDecimal.valueOf(totalBirds), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal totalFeedStock = list.stream()
                    .map(DailyReportResponse::getFeedStockBag)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPTable table = new PdfPTable(26);
            table.setWidthPercentage(100);
            table.setHeaderRows(1);
            table.setSplitRows(false);
            table.setWidths(new float[]{
                    1.8f, 3.2f, 1.8f, 3.6f, 1.8f,
                    1.8f, 2.8f, 1.8f, 1.4f,
                    1.9f, 1.7f, 1.7f, 2.2f,
                    1.8f, 1.5f, 1.6f, 1.5f,
                    1.3f, 1.3f, 1.6f, 2.4f,
                    2.4f, 1.5f, 1.9f, 2.4f, 2.4f
            });

            // Table Headers
            table.addCell(PdfTableUtil.headerCellLeft("Sch Code"));
            table.addCell(PdfTableUtil.headerCellLeft("Farmer Name"));
            table.addCell(PdfTableUtil.headerCellLeft("Farm Code"));
            table.addCell(PdfTableUtil.headerCellLeft("Customer Name"));
            table.addCell(PdfTableUtil.headerCellLeft("Cust Code"));
            table.addCell(PdfTableUtil.headerCellLeft("Sup Code"));
            table.addCell(PdfTableUtil.headerCellLeft("Supervisor Name"));
            table.addCell(PdfTableUtil.headerCellLeft("DC No"));
            table.addCell(PdfTableUtil.headerCellLeft("Slip No"));
            table.addCell(PdfTableUtil.headerCellLeft("WM Date"));
            table.addCell(PdfTableUtil.headerCellLeft("Start Time"));
            table.addCell(PdfTableUtil.headerCellLeft("End Time"));
            table.addCell(PdfTableUtil.headerCellLeft("Vehicle No"));
            table.addCell(PdfTableUtil.headerCellRight("Weight"));
            table.addCell(PdfTableUtil.headerCellRight("Birds"));
            table.addCell(PdfTableUtil.headerCellLeft("Bird Type"));
            table.addCell(PdfTableUtil.headerCellRight("Avg Wt"));
            table.addCell(PdfTableUtil.headerCellRight("Err"));
            table.addCell(PdfTableUtil.headerCellRight("Abort"));
            table.addCell(PdfTableUtil.headerCellRight("Feed Bag"));
            table.addCell(PdfTableUtil.headerCellLeft("Vehicle In"));
            table.addCell(PdfTableUtil.headerCellLeft("Vehicle Out"));
            table.addCell(PdfTableUtil.headerCellLeft("Branch"));
            table.addCell(PdfTableUtil.headerCellLeft("Sch Date"));
            table.addCell(PdfTableUtil.headerCellLeft("Delv Start"));
            table.addCell(PdfTableUtil.headerCellLeft("Delv End"));

            // Data Rows
            int index = 0;
            for (DailyReportResponse d : list) {
                Color bg = (index % 2 == 0) ? new Color(245, 245, 245) : Color.WHITE;

                table.addCell(PdfTableUtil.dataCell(d.getSchCode(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getFarmerName(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getFarmCode(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getCustomerName(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getCustomerCode(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getSupervisorCode(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getSupervisorName(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getDcNumber(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getSlipNo(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getWeighmentStartDate(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getWeighmentStartTime(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getWeighmentEndTime(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getVehicleNo(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getWeightOfBirds(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getNoOfBirds(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getBirdType(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getAvgWeight(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getErrorCount(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getAbortCount(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getFeedStockBag(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getVehicleIn(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getVehicleOut(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getBranchCode(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getSchDate(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getDeliveryStartTime(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getDeliveryEndTime(), bg));

                index++;
            }

            // Totals Row
            Color totalBg = new Color(220, 230, 241);

            PdfPCell totalLabel = PdfTableUtil.dataCellBold("TOTAL", totalBg, Element.ALIGN_RIGHT);
            totalLabel.setColspan(13);
            table.addCell(totalLabel);

            table.addCell(PdfTableUtil.dataCellBold(totalWeight, totalBg, Element.ALIGN_RIGHT));
            table.addCell(PdfTableUtil.dataCellBold(totalBirds, totalBg, Element.ALIGN_RIGHT));
            table.addCell(PdfTableUtil.dataCell("", totalBg));
            table.addCell(PdfTableUtil.dataCellBold(overallAvgWeight, totalBg, Element.ALIGN_RIGHT));

            PdfPCell midSpacer = PdfTableUtil.dataCell("", totalBg);
            midSpacer.setColspan(2);
            table.addCell(midSpacer);

            table.addCell(PdfTableUtil.dataCellBold(totalFeedStock, totalBg, Element.ALIGN_RIGHT));

            PdfPCell endSpacer = PdfTableUtil.dataCell("", totalBg);
            endSpacer.setColspan(6);
            table.addCell(endSpacer);

            document.add(table);
            document.close();

        } catch (Exception e) {
            log.error("Error while generating Daily PDF Report", e);
            throw new RuntimeException("Error while generating Daily PDF Report", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private LocalDate resolveFromDate(DailyReportFilterRequest filter) {
        if (filter == null) {
            return LocalDate.now();
        }
        return filter.getReportDate() != null
                ? filter.getReportDate()
                : (filter.getFromDate() != null ? filter.getFromDate() : LocalDate.now());
    }

    private LocalDate resolveToDate(DailyReportFilterRequest filter, LocalDate effectiveFrom) {
        if (filter == null) {
            return effectiveFrom;
        }
        return filter.getReportDate() != null
                ? filter.getReportDate()
                : (filter.getToDate() != null ? filter.getToDate() : effectiveFrom);
    }

    private void validateDateRange(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
    }

    private DailyReportResponse mapProjectionToResponse(DailyReportProjection proj) {
        return DailyReportResponse.builder()
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
                .build();
    }
}
