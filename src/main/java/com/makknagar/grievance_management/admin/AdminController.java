
package com.makknagar.grievance_management.admin;

import com.makknagar.grievance_management.complaint.Complaint;
import com.makknagar.grievance_management.complaint.ComplaintRepository;
import com.makknagar.grievance_management.complaint.ComplaintService;
import com.makknagar.grievance_management.complaint.ComplaintReport;
import com.makknagar.grievance_management.complaint.ComplaintReportRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ComplaintRepository complaintRepository;
    private final ComplaintService complaintService;
    private final ComplaintReportRepository complaintReportRepository;

    public AdminController(
            ComplaintRepository complaintRepository,
            ComplaintService complaintService,
            ComplaintReportRepository complaintReportRepository) {

        this.complaintRepository = complaintRepository;
        this.complaintService = complaintService;
        this.complaintReportRepository = complaintReportRepository;
    }

    @GetMapping("/complaints")
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    @GetMapping("/complaints/waiting-queue")
    public List<Complaint> getWaitingQueue() {
        return complaintRepository.findWaitingComplaintsInPriorityOrder();
    }

    // DEPARTMENT-WISE ANALYTICS
    @GetMapping("/analytics/departments")
    public List<Map<String, Object>> getDepartmentAnalytics() {

        List<Object[]> results =
                complaintRepository.countComplaintsByDepartment();

        List<Map<String, Object>> analytics = new ArrayList<>();

        for (Object[] result : results) {

            Map<String, Object> departmentData =
                    new LinkedHashMap<>();

            departmentData.put("department", result[0]);
            departmentData.put("totalComplaints", result[1]);

            analytics.add(departmentData);
        }

        return analytics;
    }

    // OVERALL COMPLAINT STATISTICS
    @GetMapping("/analytics/summary")
    public Map<String, Long> getOverallStatistics() {

        long totalComplaints = complaintRepository.count();

        long totalSupportingReports =
                complaintReportRepository.count();

        Map<String, Long> statistics = new LinkedHashMap<>();

        statistics.put("totalComplaints", totalComplaints);
        statistics.put("totalSupportingReports", totalSupportingReports);

        return statistics;
    }

    // GET SUPPORTING REPORTS
    @GetMapping("/complaints/{referenceNumber}/reports")
    public ResponseEntity<?> getSupportingReports(
            @PathVariable String referenceNumber) {

        Complaint complaint = complaintRepository
                .findByComplaintReferenceNumber(referenceNumber)
                .orElse(null);

        if (complaint == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Complaint not found."));
        }

        List<ComplaintReport> reports =
                complaintReportRepository
                        .findByMainComplaintIdOrderByCreatedAtAsc(
                                complaint.getId());

        List<Map<String, Object>> reportDetails = new ArrayList<>();

        for (ComplaintReport report : reports) {

            Map<String, Object> details = new LinkedHashMap<>();

            details.put("reportReferenceNumber",
                    report.getReportReferenceNumber());

            details.put("citizenReferenceNumber",
                    report.getCitizen().getCitizenReferenceNumber());

            details.put("peopleAffected",
                    report.getPeopleAffected());

            details.put("createdAt", report.getCreatedAt());

            reportDetails.add(details);
        }

        return ResponseEntity.ok(Map.of(
                "mainComplaintReferenceNumber",
                complaint.getComplaintReferenceNumber(),

                "peopleAffected",
                complaint.getPeopleAffected(),

                "supportingReportCount",
                reports.size(),

                "reports",
                reportDetails
        ));
    }

    // UPDATE COMPLAINT STATUS
    @PutMapping("/complaints/{referenceNumber}/status")
    public ResponseEntity<?> updateComplaintStatus(
            @PathVariable String referenceNumber,
            @RequestBody Map<String, String> request) {

        try {
            Complaint complaint =
                    complaintService.updateComplaintStatus(
                            referenceNumber,
                            request.get("status")
                    );

            return ResponseEntity.ok(Map.of(
                    "message", "Complaint status updated successfully!",
                    "complaintReferenceNumber",
                    complaint.getComplaintReferenceNumber(),
                    "status", complaint.getStatus()
            ));

        } catch (IllegalArgumentException exception) {

            return ResponseEntity.badRequest().body(Map.of(
                    "message", exception.getMessage()
            ));
        }
    }
}
