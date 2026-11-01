
package com.makknagar.grievance_management.complaint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Find complaint using reference number
    Optional<Complaint> findByComplaintReferenceNumber(
            String complaintReferenceNumber);

    // Find all complaints belonging to a department
    List<Complaint> findByDepartmentIgnoreCase(String department);

    // Retrieve active waiting complaints in priority order
    @Query("""
        SELECT c FROM Complaint c
        WHERE UPPER(c.assignmentStatus) = 'WAITING'
        AND UPPER(c.status) IN ('SUBMITTED', 'IN_PROGRESS')
        ORDER BY
            CASE UPPER(c.priority)
                WHEN 'HIGH' THEN 1
                WHEN 'MEDIUM' THEN 2
                WHEN 'LOW' THEN 3
                ELSE 4
            END ASC,
            c.peopleAffected DESC,
            c.createdAt ASC
        """)
    List<Complaint> findWaitingComplaintsInPriorityOrder();

    // Retrieve active waiting complaints for a particular department
    @Query("""
        SELECT c FROM Complaint c
        WHERE UPPER(c.assignmentStatus) = 'WAITING'
        AND UPPER(c.status) IN ('SUBMITTED', 'IN_PROGRESS')
        AND LOWER(c.department) = LOWER(:department)
        ORDER BY
            CASE UPPER(c.priority)
                WHEN 'HIGH' THEN 1
                WHEN 'MEDIUM' THEN 2
                WHEN 'LOW' THEN 3
                ELSE 4
            END ASC,
            c.peopleAffected DESC,
            c.createdAt ASC
        """)
    List<Complaint> findWaitingComplaintsByDepartment(
            @Param("department") String department);

    // Count active assigned complaints in a department
    @Query("""
        SELECT COUNT(c) FROM Complaint c
        WHERE LOWER(c.department) = LOWER(:department)
        AND UPPER(c.assignmentStatus) = 'ASSIGNED'
        AND UPPER(c.status) IN ('SUBMITTED', 'IN_PROGRESS')
        """)
    long countActiveAssignedComplaints(
            @Param("department") String department);

    // Find active complaints with matching category and location
    @Query("""
        SELECT c FROM Complaint c
        WHERE LOWER(TRIM(c.category)) = LOWER(TRIM(:category))
        AND LOWER(TRIM(c.location)) = LOWER(TRIM(:location))
        AND UPPER(c.status) IN ('SUBMITTED', 'IN_PROGRESS')
        ORDER BY c.createdAt ASC
        """)
    List<Complaint> findActiveComplaintsByCategoryAndLocation(
            @Param("category") String category,
            @Param("location") String location);

    // Department-wise total complaint counts
    @Query("""
        SELECT c.department, COUNT(c)
        FROM Complaint c
        GROUP BY c.department
        ORDER BY c.department
        """)
    List<Object[]> countComplaintsByDepartment();
}
