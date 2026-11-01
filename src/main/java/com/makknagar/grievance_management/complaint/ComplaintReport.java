
package com.makknagar.grievance_management.complaint;

import com.makknagar.grievance_management.citizen.Citizen;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "complaint_reports",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"report_reference_number"}
        ),
        @UniqueConstraint(
            columnNames = {"citizen_id", "main_complaint_id"}
        )
    }
)
public class ComplaintReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "report_reference_number",
        nullable = false,
        unique = true
    )
    private String reportReferenceNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "main_complaint_id", nullable = false)
    private Complaint mainComplaint;

    @Column(nullable = false)
    private Integer peopleAffected;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ComplaintReport() {
    }

    public Long getId() {
        return id;
    }

    public String getReportReferenceNumber() {
        return reportReferenceNumber;
    }

    public void setReportReferenceNumber(String reportReferenceNumber) {
        this.reportReferenceNumber = reportReferenceNumber;
    }

    public Citizen getCitizen() {
        return citizen;
    }

    public void setCitizen(Citizen citizen) {
        this.citizen = citizen;
    }

    public Complaint getMainComplaint() {
        return mainComplaint;
    }

    public void setMainComplaint(Complaint mainComplaint) {
        this.mainComplaint = mainComplaint;
    }

    public Integer getPeopleAffected() {
        return peopleAffected;
    }

    public void setPeopleAffected(Integer peopleAffected) {
        this.peopleAffected = peopleAffected;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
