
package com.makknagar.grievance_management.admin;

import com.makknagar.grievance_management.complaint.Complaint;
import com.makknagar.grievance_management.complaint.ComplaintService;
import com.makknagar.grievance_management.complaint.ComplaintRepository;
import com.makknagar.grievance_management.complaint.ComplaintReport;
import com.makknagar.grievance_management.complaint.ComplaintReportRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    private final ComplaintService complaintService;
    private final ComplaintRepository complaintRepository;
    private final ComplaintReportRepository reportRepository;

    public DepartmentController(
            ComplaintService complaintService,
            ComplaintRepository complaintRepository,
            ComplaintReportRepository reportRepository) {

        this.complaintService = complaintService;
        this.complaintRepository = complaintRepository;
        this.reportRepository = reportRepository;
    }

    private String getOfficerDepartment(Authentication authentication) {

        for (GrantedAuthority authority : authentication.getAuthorities()) {

            String role = authority.getAuthority();

            switch (role) {
                case "ROLE_WATER":
                    return "Water Supply Department";

                case "ROLE_ROADS":
                    return "Public Works Department";

                case "ROLE_ELECTRICITY":
                    return "Electricity Department";

                case "ROLE_SANITATION":
                    return "Sanitation Department";
            }
        }

        throw new IllegalArgumentException(
                "You are not authorized as a department officer.");
    }

    @GetMapping("/water/complaints")
    public ResponseEntity<List<Complaint>> getWaterComplaints() {
        return ResponseEntity.ok(
                complaintService.getDepartmentComplaints(
                        "Water Supply Department"));
    }

    @GetMapping("/roads/complaints")
    public ResponseEntity<List<Complaint>> getRoadComplaints() {
        return ResponseEntity.ok(
                complaintService.getDepartmentComplaints(
                        "Public Works Department"));
    }

    @GetMapping("/electricity/complaints")
    public ResponseEntity<List<Complaint>> getElectricityComplaints() {
        return ResponseEntity.ok(
                complaintService.getDepartmentComplaints(
                        "Electricity Department"));
    }

    @GetMapping("/sanitation/complaints")
    public ResponseEntity<List<Complaint>> getSanitationComplaints() {
        return ResponseEntity.ok(
                complaintService.getDepartmentComplaints(
                        "Sanitation Department"));
    }

    // Get supporting reports for a complaint
    @GetMapping("/complaints/{referenceNumber}/reports")
    public ResponseEntity<?> getSupportingReports(
            @PathVariable String referenceNumber,
            Authentication authentication) {

        try {
            String department = getOfficerDepartment(authentication);

            Complaint complaint = complaintRepository
                    .findByComplaintReferenceNumber(referenceNumber)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Complaint not found."));

            if (!department.equalsIgnoreCase(complaint.getDepartment())) {
                return ResponseEntity.status(403).body(
                        Map.of("message",
                                "You cannot view complaints belonging to another department."));
            }

            List<Map<String, Object>> reports =
                    reportRepository
                            .findByMainComplaintIdOrderByCreatedAtAsc(
                                    complaint.getId())
                            .stream()
                            .map(report -> Map.<String, Object>of(
                                    "reportReferenceNumber",
                                    report.getReportReferenceNumber(),
                                    "citizenReferenceNumber",
                                    report.getCitizen().getCitizenReferenceNumber(),
                                    "peopleAffected",
                                    report.getPeopleAffected(),
                                    "createdAt",
                                    report.getCreatedAt()
                            ))
                            .toList();

            return ResponseEntity.ok(Map.of(
                    "mainComplaintReferenceNumber",
                    complaint.getComplaintReferenceNumber(),
                    "peopleAffected",
                    complaint.getPeopleAffected(),
                    "supportingReportCount",
                    reports.size(),
                    "reports",
                    reports
            ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage()));
        }
    }

    @PutMapping("/complaints/{referenceNumber}/status")
    public ResponseEntity<?> updateDepartmentComplaintStatus(
            @PathVariable String referenceNumber,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        try {
            String department = getOfficerDepartment(authentication);

            Complaint complaint =
                    complaintService.updateDepartmentComplaintStatus(
                            referenceNumber,
                            request.get("status"),
                            department);

            return ResponseEntity.ok(Map.of(
                    "message", "Complaint status updated successfully!",
                    "complaintReferenceNumber",
                    complaint.getComplaintReferenceNumber(),
                    "status", complaint.getStatus()
            ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage()));
        }
    }
}
