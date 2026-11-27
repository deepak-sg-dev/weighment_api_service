package com.suguna.weighment_api_service.admin.utils;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;

import java.awt.Color;

public class PdfTableUtil {

    private static final Color HEADER_TEAL = new Color(0, 121, 107);

    public static PdfPCell headerCell(String text) {
        Font font = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(HEADER_TEAL);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4.5f);
        return cell;
    }

    public static PdfPCell headerCellLeft(String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(text,
                        new Font(Font.HELVETICA, 8.5f, Font.BOLD, Color.WHITE)));

        cell.setBackgroundColor(HEADER_TEAL);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4.5f);

        return cell;
    }

    public static PdfPCell headerCellRight(String text) {
        PdfPCell cell = new PdfPCell(
                new Phrase(text,
                        new Font(Font.HELVETICA, 8.5f, Font.BOLD, Color.WHITE)));

        cell.setBackgroundColor(HEADER_TEAL);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4.5f);

        return cell;
    }

    public static PdfPCell dataCell(Object value, Color bg) {
        Font font = new Font(Font.HELVETICA, 8.3f);

        PdfPCell cell = new PdfPCell(
                new Phrase(value != null ? value.toString() : "-", font)
        );

        cell.setBackgroundColor(bg);
        cell.setPadding(3.0f);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        return cell;
    }

    public static PdfPCell dataCellRight(Object value, Color bg) {
        Font font = new Font(Font.HELVETICA, 8.3f);

        PdfPCell cell = new PdfPCell(
                new Phrase(value != null ? value.toString() : "-", font)
        );

        cell.setBackgroundColor(bg);
        cell.setPadding(3.0f);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        return cell;
    }

    public static PdfPCell dataCellBold(Object value, Color bg, int alignment) {
        Font font = new Font(Font.HELVETICA, 8.5f, Font.BOLD);

        PdfPCell cell = new PdfPCell(
                new Phrase(value != null ? value.toString() : "-", font)
        );

        cell.setBackgroundColor(bg);
        cell.setPadding(4.0f);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        return cell;
    }
}
