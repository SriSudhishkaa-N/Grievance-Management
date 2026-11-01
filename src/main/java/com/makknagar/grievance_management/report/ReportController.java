
package com.makknagar.grievance_management.report;

import com.makknagar.grievance_management.complaint.Complaint;
import com.makknagar.grievance_management.complaint.ComplaintRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ComplaintRepository complaintRepository;
    private final PdfReportService pdfReportService;

    public ReportController(
            ComplaintRepository complaintRepository,
            PdfReportService pdfReportService) {

        this.complaintRepository = complaintRepository;
        this.pdfReportService = pdfReportService;
    }

    // ADMIN PDF REPORT
    @GetMapping("/admin/reports/pdf")
    public ResponseEntity<byte[]> adminReport(

            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String department) {

        return generatePdf(
                department,
                startDate,
                endDate,
                status,
                priority
        );
    }

    // DEPARTMENT PDF REPORT
    @GetMapping("/department/reports/pdf")
    public ResponseEntity<byte[]> departmentReport(

            Authentication authentication,

            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {

        // Department is determined from the logged-in officer.
        // Officers cannot select another department.
        String department = getOfficerDepartment(authentication);

        return generatePdf(
                department,
                startDate,
                endDate,
                status,
                priority
        );
    }

    // GENERATE FILTERED PDF
    private ResponseEntity<byte[]> generatePdf(

            String department,
            String startDate,
            String endDate,
            String status,
            String priority) {

        List<Complaint> complaints =
                complaintRepository.findAll();

        // DEPARTMENT FILTER
        if (department != null && !department.isBlank()) {

            complaints = complaints.stream()
                    .filter(c -> c.getDepartment() != null
                            && department.equalsIgnoreCase(
                                    c.getDepartment().trim()))
                    .collect(Collectors.toList());
        }

        // START DATE FILTER
        if (startDate != null && !startDate.isBlank()) {

            LocalDateTime start =
                    LocalDate.parse(startDate).atStartOfDay();

            complaints = complaints.stream()
                    .filter(c -> c.getCreatedAt() != null
                            && !c.getCreatedAt().isBefore(start))
                    .collect(Collectors.toList());
        }

        // END DATE FILTER
        if (endDate != null && !endDate.isBlank()) {

            LocalDateTime end =
                    LocalDate.parse(endDate)
                            .plusDays(1)
                            .atStartOfDay();

            complaints = complaints.stream()
                    .filter(c -> c.getCreatedAt() != null
                            && c.getCreatedAt().isBefore(end))
                    .collect(Collectors.toList());
        }

        // STATUS FILTER
        if (status != null && !status.isBlank()) {

            complaints = complaints.stream()
                    .filter(c -> status.equalsIgnoreCase(
                            c.getStatus()))
                    .collect(Collectors.toList());
        }

        // PRIORITY FILTER
        if (priority != null && !priority.isBlank()) {

            complaints = complaints.stream()
                    .filter(c -> priority.equalsIgnoreCase(
                            c.getPriority()))
                    .collect(Collectors.toList());
        }

        // REPORT DEPARTMENT NAME
        String departmentName =
                department == null || department.isBlank()
                        ? "All Departments"
                        : department;

        // REPORT PERIOD
        String period =
                (startDate == null || startDate.isBlank()
                        ? "Beginning" : startDate)
                + " to "
                + (endDate == null || endDate.isBlank()
                        ? "Present" : endDate);

        // GENERATE PDF
        byte[] pdf = pdfReportService.generateReport(
                complaints,
                departmentName,
                period
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=grievance-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // IDENTIFY LOGGED-IN DEPARTMENT OFFICER
    private String getOfficerDepartment(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "Authentication required");
        }

        boolean water = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_WATER"));

        boolean roads = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ROADS"));

        boolean electricity = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ELECTRICITY"));

        boolean sanitation = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SANITATION"));

        if (water) return "Water Supply Department";
        if (roads) return "Public Works Department";
        if (electricity) return "Electricity Department";
        if (sanitation) return "Sanitation Department";

        throw new org.springframework.security.access.AccessDeniedException(
                "Department officer access required");
    }
}
