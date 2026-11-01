
package com.makknagar.grievance_management.complaint;

import com.makknagar.grievance_management.citizen.Citizen;
import com.makknagar.grievance_management.citizen.CitizenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private static final int DEPARTMENT_CAPACITY = 5;

    private final ComplaintRepository complaintRepository;
    private final CitizenRepository citizenRepository;
    private final ComplaintReportRepository reportRepository;

    public ComplaintService(
            ComplaintRepository complaintRepository,
            CitizenRepository citizenRepository,
            ComplaintReportRepository reportRepository) {

        this.complaintRepository = complaintRepository;
        this.citizenRepository = citizenRepository;
        this.reportRepository = reportRepository;
    }

    // Submit complaint with automatic duplicate detection
    @Transactional
    public ComplaintSubmissionResult submitComplaint(
            String citizenReferenceNumber,
            String title,
            String description,
            String category,
            String location,
            String peopleAffectedText) {

        if (citizenReferenceNumber == null
                || citizenReferenceNumber.isBlank()
                || title == null || title.isBlank()
                || description == null || description.isBlank()
                || category == null || category.isBlank()
                || location == null || location.isBlank()
                || peopleAffectedText == null
                || peopleAffectedText.isBlank()) {

            throw new IllegalArgumentException(
                    "All fields are required.");
        }

        int peopleAffected;

        try {
            peopleAffected = Integer.parseInt(
                    peopleAffectedText.trim());

        } catch (NumberFormatException exception) {

            throw new IllegalArgumentException(
                    "Enter a valid number of people affected.");
        }

        if (peopleAffected < 1) {
            throw new IllegalArgumentException(
                    "Number of people affected must be at least 1.");
        }

        Citizen citizen = citizenRepository
                .findByCitizenReferenceNumber(
                        citizenReferenceNumber.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid citizen reference number."));

        // Search active complaints with matching category and location
        List<Complaint> candidates =
                complaintRepository
                        .findActiveComplaintsByCategoryAndLocation(
                                category.trim(),
                                location.trim());

        String submittedIssue = title + " " + description;

        for (Complaint existing : candidates) {

            String existingIssue =
                    existing.getTitle() + " "
                    + existing.getDescription();

            if (!isSimilarIssue(submittedIssue, existingIssue)) {
                continue;
            }

            // Check whether this citizen already reported this issue
            var previousReport =
                    reportRepository
                            .findByCitizenIdAndMainComplaintId(
                                    citizen.getId(),
                                    existing.getId());

            if (previousReport.isPresent()) {

                return new ComplaintSubmissionResult(
                        existing,
                        previousReport.get()
                                .getReportReferenceNumber(),
                        "Duplicate Complaint Detected. "
                        + "You have already submitted this complaint. "
                        + "Your existing Complaint ID is "
                        + previousReport.get()
                                .getReportReferenceNumber()
                        + ". Please use this reference number "
                        + "to track your complaint.",
                        true
                );
            }

            // Save another citizen's report against the same issue
            ComplaintReport report = new ComplaintReport();

            report.setCitizen(citizen);
            report.setMainComplaint(existing);
            report.setPeopleAffected(peopleAffected);
            report.setReportReferenceNumber(
                    generateReportReference());

            reportRepository.save(report);

            // Keep the highest reported affected-people estimate
            if (peopleAffected > existing.getPeopleAffected()) {

                existing.setPeopleAffected(peopleAffected);

                existing.setPriority(
                        calculatePriority(peopleAffected));

                complaintRepository.save(existing);
            }

            return new ComplaintSubmissionResult(
                    existing,
                    report.getReportReferenceNumber(),
                    "Your report has been linked to an existing "
                    + "complaint. Thank you for supporting this report.",
                    false
            );
        }

        // No matching active issue: create a new main complaint
        Complaint complaint = new Complaint();

        complaint.setCitizen(citizen);
        complaint.setTitle(title.trim());
        complaint.setDescription(description.trim());
        complaint.setCategory(category.trim());
        complaint.setLocation(location.trim());
        complaint.setPeopleAffected(peopleAffected);
        complaint.setPriority(calculatePriority(peopleAffected));
        complaint.setDepartment(
                assignDepartment(category, description));

        complaint.setComplaintReferenceNumber(
                generateReportReference());

        // Check department capacity
        long activeComplaints =
                complaintRepository.countActiveAssignedComplaints(
                        complaint.getDepartment());

        if (!complaint.getDepartment().equalsIgnoreCase(
                    "Admin Review Required")
                && activeComplaints < DEPARTMENT_CAPACITY) {

            complaint.setAssignmentStatus("ASSIGNED");

        } else {

            complaint.setAssignmentStatus("WAITING");
        }

        Complaint savedComplaint =
                complaintRepository.save(complaint);

        // Create the first citizen's individual report
        ComplaintReport firstReport = new ComplaintReport();

        firstReport.setCitizen(citizen);
        firstReport.setMainComplaint(savedComplaint);
        firstReport.setPeopleAffected(peopleAffected);

        // Preserve the original complaint reference for the first citizen
        firstReport.setReportReferenceNumber(
                savedComplaint.getComplaintReferenceNumber());

        reportRepository.save(firstReport);

        return new ComplaintSubmissionResult(
                savedComplaint,
                firstReport.getReportReferenceNumber(),
                "Complaint submitted successfully!",
                false
        );
    }

    // Generate a unique tracking reference
    private String generateReportReference() {

        return "GRV-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }

    // Calculate priority
    private String calculatePriority(int peopleAffected) {

        if (peopleAffected <= 10) {
            return "LOW";
        } else if (peopleAffected <= 50) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }

    // Compare issue descriptions using meaningful word overlap
    private boolean isSimilarIssue(
            String firstIssue,
            String secondIssue) {

        Set<String> firstWords = extractKeywords(firstIssue);
        Set<String> secondWords = extractKeywords(secondIssue);

        if (firstWords.size() < 2 || secondWords.size() < 2) {
            return false;
        }

        Set<String> commonWords =
                new HashSet<>(firstWords);

        commonWords.retainAll(secondWords);

        if (commonWords.size() < 2) {
            return false;
        }

        // Compare shared words against the smaller keyword set
        double similarity =
                (double) commonWords.size()
                / Math.min(firstWords.size(), secondWords.size());

        return similarity >= 0.60;
    }

    // Extract meaningful words and remove common words
    private Set<String> extractKeywords(String text) {

        Set<String> stopWords = Set.of(
                "the", "is", "are", "was", "were",
                "a", "an", "and", "or", "of",
                "in", "on", "at", "to", "for",
                "near", "with", "from", "by",
                "has", "have", "been", "being",
                "this", "that", "there", "their",
                "our", "my", "we", "they",
                "issue", "problem", "please",
                "request", "kindly"
        );

        return Arrays.stream(
                    text.toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9 ]", " ")
                        .split("\\s+"))
                .filter(word -> word.length() > 2)
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.toSet());
    }

    // Admin updates complaint status
    @Transactional
    public Complaint updateComplaintStatus(
            String complaintReferenceNumber,
            String newStatus) {

        String status = validateStatus(newStatus);

        Complaint complaint = complaintRepository
                .findByComplaintReferenceNumber(
                        complaintReferenceNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Complaint not found."));

        complaint.setStatus(status);

        if (status.equals("RESOLVED")) {
            complaint.setAssignmentStatus("CLOSED");
        }

        Complaint savedComplaint =
                complaintRepository.save(complaint);

        if (status.equals("RESOLVED")) {
            promoteWaitingComplaint(
                    complaint.getDepartment());
        }

        return savedComplaint;
    }

    // Department officer updates their own complaints
    @Transactional
    public Complaint updateDepartmentComplaintStatus(
            String complaintReferenceNumber,
            String newStatus,
            String department) {

        String status = validateStatus(newStatus);

        Complaint complaint = complaintRepository
                .findByComplaintReferenceNumber(
                        complaintReferenceNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Complaint not found."));

        if (complaint.getDepartment() == null
                || !complaint.getDepartment()
                        .equalsIgnoreCase(department)) {

            throw new IllegalArgumentException(
                    "You cannot update complaints belonging "
                    + "to another department.");
        }

        complaint.setStatus(status);

        if (status.equals("RESOLVED")) {
            complaint.setAssignmentStatus("CLOSED");
        }

        Complaint savedComplaint =
                complaintRepository.save(complaint);

        if (status.equals("RESOLVED")) {
            promoteWaitingComplaint(
                    complaint.getDepartment());
        }

        return savedComplaint;
    }

    // Validate complaint status
    private String validateStatus(String newStatus) {

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Complaint status is required.");
        }

        String status = newStatus.trim().toUpperCase();

        Set<String> allowedStatuses = Set.of(
                "SUBMITTED",
                "IN_PROGRESS",
                "RESOLVED"
        );

        if (!allowedStatuses.contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid status. Choose SUBMITTED, "
                    + "IN_PROGRESS or RESOLVED.");
        }

        return status;
    }

    // Promote waiting complaints when capacity is available
    private void promoteWaitingComplaint(String department) {

        if (department == null
                || department.isBlank()
                || department.equalsIgnoreCase(
                        "Admin Review Required")) {

            return;
        }

        while (complaintRepository
                .countActiveAssignedComplaints(department)
                < DEPARTMENT_CAPACITY) {

            List<Complaint> waitingComplaints =
                    complaintRepository
                            .findWaitingComplaintsByDepartment(
                                    department);

            if (waitingComplaints.isEmpty()) {
                break;
            }

            Complaint nextComplaint =
                    waitingComplaints.get(0);

            nextComplaint.setAssignmentStatus("ASSIGNED");

            complaintRepository.save(nextComplaint);
        }
    }

    // Fetch complaints belonging to one department
    public List<Complaint> getDepartmentComplaints(
            String department) {

        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException(
                    "Department name is required.");
        }

        return complaintRepository.findByDepartmentIgnoreCase(
                department.trim());
    }

    // Automatic department identification
    private String assignDepartment(
            String category,
            String description) {

        String categoryText =
                category.trim().toLowerCase(Locale.ROOT);

        String text = (
                category + " " + description
        ).toLowerCase(Locale.ROOT);

        if (categoryText.contains("sanitation")
                || text.contains("garbage")
                || text.contains("waste")
                || text.contains("dustbin")
                || text.contains("drainage")
                || text.contains("sewage")
                || text.contains("blocked drain")
                || text.contains("wastewater")) {

            return "Sanitation Department";
        }

        if (text.contains("water")
                || text.contains("leakage")
                || text.contains("pipeline")
                || text.contains("drinking water")) {

            return "Water Supply Department";
        }

        if (text.contains("road")
                || text.contains("pothole")
                || text.contains("highway")
                || text.contains("street damage")) {

            return "Public Works Department";
        }

        if (text.contains("electricity")
                || text.contains("power cut")
                || text.contains("power failure")
                || text.contains("electric pole")
                || text.contains("street light")
                || text.contains("streetlight")
                || text.contains("lamp")) {

            return "Electricity Department";
        }

        return "Admin Review Required";
    }
}
