package com.suguna.weighment_api_service.admin.utils;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonPdfPageEvent extends PdfPageEventHelper {

    private final String reportTitle;
    private final String subtitle;
    private final String asOnDate;

    public CommonPdfPageEvent(String reportTitle, String subtitle) {
        this.reportTitle = reportTitle;
        this.subtitle = subtitle;
        this.asOnDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        try {
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);

            Font companyFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font subFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            PdfPCell companyCell = new PdfPCell(
                    new Phrase("SUGUNA FOODS PVT LTD", companyFont));
            companyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            companyCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(companyCell);

            if (subtitle != null && !subtitle.isBlank()) {
                PdfPCell subCell = new PdfPCell(new Phrase(subtitle, subFont));
                subCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                subCell.setBorder(Rectangle.NO_BORDER);
                headerTable.addCell(subCell);
            }

            document.add(headerTable);
            document.add(Chunk.NEWLINE);

            // Report Title (Left) + As On Date (Right)
            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);

            PdfPCell left = new PdfPCell(new Phrase(reportTitle,
                    new Font(Font.HELVETICA, 12, Font.BOLD)));
            left.setBorder(Rectangle.NO_BORDER);

            PdfPCell right = new PdfPCell(new Phrase("As On: " + asOnDate, subFont));
            right.setHorizontalAlignment(Element.ALIGN_RIGHT);
            right.setBorder(Rectangle.NO_BORDER);

            meta.addCell(left);
            meta.addCell(right);

            document.add(meta);
            document.add(new Paragraph(" "));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();
        Font footerFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);

        Phrase footer = new Phrase(
                "Page " + writer.getPageNumber(),
                footerFont
        );

        ColumnText.showTextAligned(
                cb,
                Element.ALIGN_CENTER,
                footer,
                (document.right() + document.left()) / 2,
                document.bottom() - 15,
                0
        );
    }
}
