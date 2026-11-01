
package com.makknagar.grievance_management.complaint;

public class ComplaintSubmissionResult {

    private final Complaint complaint;
    private final String reportReferenceNumber;
    private final String message;
    private final boolean duplicate;

    public ComplaintSubmissionResult(
            Complaint complaint,
            String reportReferenceNumber,
            String message,
            boolean duplicate) {

        this.complaint = complaint;
        this.reportReferenceNumber = reportReferenceNumber;
        this.message = message;
        this.duplicate = duplicate;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public String getReportReferenceNumber() {
        return reportReferenceNumber;
    }

    public String getMessage() {
        return message;
    }

    public boolean isDuplicate() {
        return duplicate;
    }
}
