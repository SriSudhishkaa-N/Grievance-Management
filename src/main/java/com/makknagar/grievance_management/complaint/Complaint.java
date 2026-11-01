
package com.makknagar.grievance_management.complaint;

import com.makknagar.grievance_management.citizen.Citizen;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String complaintReferenceNumber;

    @ManyToOne
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String status = "SUBMITTED";

    @Column(nullable = false)
    private Integer peopleAffected;

    @Column(nullable = false)
    private String priority;

    @Column(nullable = false)
    private String department;

    // WAITING or ASSIGNED
    @Column(nullable = false)
    private String assignmentStatus = "WAITING";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Complaint() {
    }

    public Long getId() {
        return id;
    }

    public String getComplaintReferenceNumber() {
        return complaintReferenceNumber;
    }

    public void setComplaintReferenceNumber(String complaintReferenceNumber) {
        this.complaintReferenceNumber = complaintReferenceNumber;
    }

    public Citizen getCitizen() {
        return citizen;
    }

    public void setCitizen(Citizen citizen) {
        this.citizen = citizen;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPeopleAffected() {
        return peopleAffected;
    }

    public void setPeopleAffected(Integer peopleAffected) {
        this.peopleAffected = peopleAffected;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(String assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
