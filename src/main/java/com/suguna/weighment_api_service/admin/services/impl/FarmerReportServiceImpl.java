package com.suguna.weighment_api_service.admin.services.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.suguna.weighment_api_service.admin.Projection.FarmerReportProjection;
import com.suguna.weighment_api_service.admin.dtos.requests.FarmerReportFilterRequest;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerReportListResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.FarmerReportResponse;
import com.suguna.weighment_api_service.admin.dtos.responses.PaginationDTO;
import com.suguna.weighment_api_service.admin.repositories.FarmerReportRepository;
import com.suguna.weighment_api_service.admin.services.FarmerReportService;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmerReportServiceImpl implements FarmerReportService {

    private final FarmerReportRepository farmerReportRepository;

    @Override
    @Transactional(readOnly = true)
    public FarmerReportListResponse getFarmerReport(FarmerReportFilterRequest filter) {
        validateDateRange(filter);

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
                .map(this::mapProjectionToResponse)
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

    @Override
    @Transactional(readOnly = true)
    public List<FarmerReportResponse> getAllFarmerReportForExport(FarmerReportFilterRequest filter) {
        validateDateRange(filter);

        return farmerReportRepository.findAllFarmerReport(
                        filter.getFromDate(),
                        filter.getToDate(),
                        filter.getBranchCode(),
                        filter.getFarmCode()
                )
                .stream()
                .map(this::mapProjectionToResponse)
                .toList();
    }

    @Override
    public ByteArrayInputStream exportFarmerReportToExcel(
            List<FarmerReportResponse> list,
            FarmerReportFilterRequest filter) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Farmer Report");
            int rowIdx = 0;

            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);

            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);

            // Title Row
            Row titleRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("FARMER WEIGHMENT REPORT");
            titleCell.setCellStyle(boldStyle);

            if (filter != null && filter.getFromDate() != null && filter.getToDate() != null) {
                Row periodRow = sheet.createRow(rowIdx++);
                periodRow.createCell(0).setCellValue(
                        "Period: " + filter.getFromDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                + " to " + filter.getToDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                );
            }

            rowIdx++;

            // Summary Calculations
            BigDecimal totalWeight = list.stream()
                    .map(FarmerReportResponse::getWeight)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalBirds = list.stream()
                    .map(FarmerReportResponse::getBirds)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            BigDecimal overallAvgWeight = totalBirds > 0
                    ? totalWeight.divide(BigDecimal.valueOf(totalBirds), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

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

            rowIdx++;

            // Table Headers
            String[] headers = {
                    "Site", "Customer", "Supervisor", "DC No", "Start Date",
                    "Start Time", "End Time", "Vehicle No", "Weight (Kgs)",
                    "Birds", "Avg Weight", "Err Count", "Sch Date"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(boldStyle);
            }

            // Data Rows
            for (FarmerReportResponse d : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getSite() != null ? d.getSite() : "");
                row.createCell(1).setCellValue(d.getCustomer() != null ? d.getCustomer() : "");
                row.createCell(2).setCellValue(d.getSupervisor() != null ? d.getSupervisor() : "");
                row.createCell(3).setCellValue(d.getDcNo() != null ? d.getDcNo() : "");
                row.createCell(4).setCellValue(d.getStartDate() != null ? d.getStartDate() : "");
                row.createCell(5).setCellValue(d.getStartTime() != null ? d.getStartTime() : "");
                row.createCell(6).setCellValue(d.getEndTime() != null ? d.getEndTime() : "");
                row.createCell(7).setCellValue(d.getVehicleNo() != null ? d.getVehicleNo() : "");
                row.createCell(8).setCellValue(d.getWeight() != null ? d.getWeight().doubleValue() : 0.0);
                row.createCell(9).setCellValue(d.getBirds() != null ? d.getBirds() : 0);
                row.createCell(10).setCellValue(d.getAvgWeight() != null ? d.getAvgWeight().doubleValue() : 0.0);
                row.createCell(11).setCellValue(d.getErrCount() != null ? d.getErrCount() : 0);
                row.createCell(12).setCellValue(d.getSchDate() != null ? d.getSchDate() : "");
            }

            // Totals Row
            Row totalRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell totalLbl = totalRow.createCell(7);
            totalLbl.setCellValue("TOTAL");
            totalLbl.setCellStyle(boldStyle);

            org.apache.poi.ss.usermodel.Cell totalWtCell = totalRow.createCell(8);
            totalWtCell.setCellValue(totalWeight.doubleValue());
            totalWtCell.setCellStyle(boldStyle);

            org.apache.poi.ss.usermodel.Cell totalBirdsCell = totalRow.createCell(9);
            totalBirdsCell.setCellValue(totalBirds);
            totalBirdsCell.setCellStyle(boldStyle);

            org.apache.poi.ss.usermodel.Cell avgWtCell = totalRow.createCell(10);
            avgWtCell.setCellValue(overallAvgWeight.doubleValue());
            avgWtCell.setCellStyle(boldStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Error while generating Farmer Excel Report", e);
            throw new RuntimeException("Error while generating Farmer Excel Report", e);
        }
    }

    @Override
    public ByteArrayInputStream exportFarmerReportToPdf(
            List<FarmerReportResponse> list,
            FarmerReportFilterRequest filter) {

        Document document = new Document(PageSize.A4.rotate(), 10, 10, 12, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);

            String subtitle = "";
            if (filter != null && filter.getFromDate() != null && filter.getToDate() != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                subtitle = "Period: " + filter.getFromDate().format(fmt) + " to " + filter.getToDate().format(fmt);
            }

            writer.setPageEvent(new CommonPdfPageEvent("FARMER WEIGHMENT REPORT", subtitle));
            document.open();

            BigDecimal totalWeight = list.stream()
                    .map(FarmerReportResponse::getWeight)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalBirds = list.stream()
                    .map(FarmerReportResponse::getBirds)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            BigDecimal overallAvgWeight = totalBirds > 0
                    ? totalWeight.divide(BigDecimal.valueOf(totalBirds), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            PdfPTable table = new PdfPTable(13);
            table.setWidthPercentage(100);
            table.setHeaderRows(1);
            table.setSplitRows(false);
            table.setWidths(new float[]{1.6f, 8.2f, 5.6f, 1.7f, 2.0f, 1.3f, 1.3f, 2.3f, 1.6f, 1.3f, 1.5f, 1.2f, 1.8f});

            // Table Headers
            table.addCell(PdfTableUtil.headerCellLeft("Site"));
            table.addCell(PdfTableUtil.headerCellLeft("Customer"));
            table.addCell(PdfTableUtil.headerCellLeft("Supervisor"));
            table.addCell(PdfTableUtil.headerCellLeft("DC No"));
            table.addCell(PdfTableUtil.headerCellLeft("Start Date"));
            table.addCell(PdfTableUtil.headerCellLeft("Start Time"));
            table.addCell(PdfTableUtil.headerCellLeft("End Time"));
            table.addCell(PdfTableUtil.headerCellLeft("Vehicle No"));
            table.addCell(PdfTableUtil.headerCellRight("Weight"));
            table.addCell(PdfTableUtil.headerCellRight("Birds"));
            table.addCell(PdfTableUtil.headerCellRight("Avg Wt"));
            table.addCell(PdfTableUtil.headerCellRight("Err Cnt"));
            table.addCell(PdfTableUtil.headerCellLeft("Sch Date"));

            // Data Rows
            int index = 0;
            for (FarmerReportResponse d : list) {
                Color bg = (index % 2 == 0) ? new Color(245, 245, 245) : Color.WHITE;

                table.addCell(PdfTableUtil.dataCell(d.getSite(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getCustomer(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getSupervisor(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getDcNo(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getStartDate(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getStartTime(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getEndTime(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getVehicleNo(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getWeight(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getBirds(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getAvgWeight(), bg));
                table.addCell(PdfTableUtil.dataCellRight(d.getErrCount(), bg));
                table.addCell(PdfTableUtil.dataCell(d.getSchDate(), bg));

                index++;
            }

            // Totals Row
            Color totalBg = new Color(220, 230, 241);

            PdfPCell totalLabel = PdfTableUtil.dataCellBold("TOTAL", totalBg, Element.ALIGN_RIGHT);
            totalLabel.setColspan(8);
            table.addCell(totalLabel);

            table.addCell(PdfTableUtil.dataCellBold(totalWeight, totalBg, Element.ALIGN_RIGHT));
            table.addCell(PdfTableUtil.dataCellBold(totalBirds, totalBg, Element.ALIGN_RIGHT));
            table.addCell(PdfTableUtil.dataCellBold(overallAvgWeight, totalBg, Element.ALIGN_RIGHT));

            PdfPCell endSpacer = PdfTableUtil.dataCell("", totalBg);
            endSpacer.setColspan(2);
            table.addCell(endSpacer);

            document.add(table);
            document.close();

        } catch (Exception e) {
            log.error("Error while generating Farmer PDF Report", e);
            throw new RuntimeException("Error while generating Farmer PDF Report", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void validateDateRange(FarmerReportFilterRequest filter) {
        if (filter.getFromDate() != null && filter.getToDate() != null
                && filter.getFromDate().isAfter(filter.getToDate())) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
    }

    private FarmerReportResponse mapProjectionToResponse(FarmerReportProjection proj) {
        return FarmerReportResponse.builder()
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
                .build();
    }
}
