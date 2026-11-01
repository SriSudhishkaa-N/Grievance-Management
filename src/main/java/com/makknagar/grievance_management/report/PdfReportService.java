package com.makknagar.grievance_management.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.makknagar.grievance_management.complaint.Complaint;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportService {

    public byte[] generateReport(
            List<Complaint> complaints,
            String department,
            String period) {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 18);

            Font headingFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 12);

            Font normalFont = FontFactory.getFont(
                    FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph(
                    "Government Grievance Management and Priority Resolution System",
                    titleFont);

            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "Department: " + department, headingFont));

            document.add(new Paragraph(
                    "Report Period: " + period, normalFont));

            document.add(new Paragraph(
                    "Generated Date: " +
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern(
                                    "dd-MM-yyyy HH:mm")),
                    normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "Total Complaints: " + complaints.size(),
                    headingFont));

            long high = complaints.stream()
                    .filter(c -> "HIGH".equalsIgnoreCase(c.getPriority()))
                    .count();

            long medium = complaints.stream()
                    .filter(c -> "MEDIUM".equalsIgnoreCase(c.getPriority()))
                    .count();

            long low = complaints.stream()
                    .filter(c -> "LOW".equalsIgnoreCase(c.getPriority()))
                    .count();

            document.add(new Paragraph(
                    "High Priority: " + high, normalFont));

            document.add(new Paragraph(
                    "Medium Priority: " + medium, normalFont));

            document.add(new Paragraph(
                    "Low Priority: " + low, normalFont));

            document.add(new Paragraph(" "));

            String[] statuses = {
                    "SUBMITTED", "IN_PROGRESS", "RESOLVED"
            };

            document.add(new Paragraph(
                    "Status Summary", headingFont));

            for (String status : statuses) {
                long count = complaints.stream()
                        .filter(c -> status.equalsIgnoreCase(c.getStatus()))
                        .count();

                document.add(new Paragraph(
                        status + ": " + count, normalFont));
            }

            long waiting = complaints.stream()
                    .filter(c -> "SUBMITTED".equalsIgnoreCase(c.getStatus()))
                    .count();

            document.add(new Paragraph(
                    "Waiting Complaints: " + waiting, normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "Detailed Complaint List", headingFont));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);

            String[] headers = {
                    "Reference",
                    "Title",
                    "Category",
                    "Location",
                    "People Affected",
                    "Priority",
                    "Department",
                    "Status"
            };

            for (String header : headers) {
                table.addCell(new Phrase(header, headingFont));
            }

            for (Complaint complaint : complaints) {

                table.addCell(value(complaint.getComplaintReferenceNumber()));
                table.addCell(value(complaint.getTitle()));
                table.addCell(value(complaint.getCategory()));
                table.addCell(value(complaint.getLocation()));
                table.addCell(String.valueOf(complaint.getPeopleAffected()));
                table.addCell(value(complaint.getPriority()));
                table.addCell(value(complaint.getDepartment()));
                table.addCell(value(complaint.getStatus()));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException exception) {
        throw new RuntimeException(
                "Unable to generate PDF report.", exception);
        }
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}