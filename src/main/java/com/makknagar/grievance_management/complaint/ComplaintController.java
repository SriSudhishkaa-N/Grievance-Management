
package com.makknagar.grievance_management.complaint;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ComplaintRepository complaintRepository;
    private final ComplaintReportRepository reportRepository;

    public ComplaintController(
            ComplaintService complaintService,
            ComplaintRepository complaintRepository,
            ComplaintReportRepository reportRepository) {

        this.complaintService = complaintService;
        this.complaintRepository = complaintRepository;
        this.reportRepository = reportRepository;
    }

    // Submit a complaint
    @PostMapping("/submit")
    public ResponseEntity<?> submitComplaint(
            @RequestBody Map<String, String> request) {

        try {
            ComplaintSubmissionResult result =
                    complaintService.submitComplaint(
                            request.get("citizenReferenceNumber"),
                            request.get("title"),
                            request.get("description"),
                            request.get("category"),
                            request.get("location"),
                            request.get("peopleAffected")
                    );

            Complaint complaint = result.getComplaint();

            return ResponseEntity
                    .status(result.isDuplicate()
                            ? HttpStatus.OK
                            : HttpStatus.CREATED)
                    .body(Map.of(
                            "message", result.getMessage(),
                            "complaintReferenceNumber",
                            result.getReportReferenceNumber(),
                            "mainComplaintReferenceNumber",
                            complaint.getComplaintReferenceNumber(),
                            "status", complaint.getStatus(),
                            "peopleAffected",
                            complaint.getPeopleAffected(),
                            "priority", complaint.getPriority(),
                            "department", complaint.getDepartment(),
                            "duplicate", result.isDuplicate()
                    ));

        } catch (IllegalArgumentException exception) {

            return ResponseEntity.badRequest().body(Map.of(
                    "message", exception.getMessage()
            ));
        }
    }

    // Track an individual citizen report
    @GetMapping("/track/{referenceNumber}")
    public ResponseEntity<?> trackComplaint(
            @PathVariable String referenceNumber) {

        return reportRepository
                .findByReportReferenceNumber(referenceNumber.trim())
                .<ResponseEntity<?>>map(report -> {

                    Complaint complaint =
                            report.getMainComplaint();

                    return ResponseEntity.ok(Map.of(
                            "complaintReferenceNumber",
                            report.getReportReferenceNumber(),
                            "mainComplaintReferenceNumber",
                            complaint.getComplaintReferenceNumber(),
                            "title", complaint.getTitle(),
                            "category", complaint.getCategory(),
                            "location", complaint.getLocation(),
                            "status", complaint.getStatus(),
                            "priority", complaint.getPriority(),
                            "department", complaint.getDepartment(),
                            "peopleAffected",
                            complaint.getPeopleAffected()
                    ));
                })
                .orElseGet(() ->
                        // Backward compatibility with old complaint references
                        complaintRepository
                                .findByComplaintReferenceNumber(
                                        referenceNumber.trim())
                                .<ResponseEntity<?>>map(complaint ->
                                        ResponseEntity.ok(Map.of(
                                                "complaintReferenceNumber",
                                                complaint.getComplaintReferenceNumber(),
                                                "title", complaint.getTitle(),
                                                "category", complaint.getCategory(),
                                                "location", complaint.getLocation(),
                                                "status", complaint.getStatus(),
                                                "priority", complaint.getPriority(),
                                                "department", complaint.getDepartment(),
                                                "peopleAffected",
                                                complaint.getPeopleAffected()
                                        )))
                                .orElseGet(() ->
                                        ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(Map.of(
                                                        "message",
                                                        "Complaint not found."
                                                )))
                );
    }
}
