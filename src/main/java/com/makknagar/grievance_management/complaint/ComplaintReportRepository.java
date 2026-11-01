
package com.makknagar.grievance_management.complaint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ComplaintReportRepository
        extends JpaRepository<ComplaintReport, Long> {

    Optional<ComplaintReport> findByReportReferenceNumber(
            String reportReferenceNumber
    );

    Optional<ComplaintReport> findByCitizenIdAndMainComplaintId(
            Long citizenId,
            Long mainComplaintId
    );

    boolean existsByCitizenIdAndMainComplaintId(
            Long citizenId,
            Long mainComplaintId
    );

    long countByMainComplaintId(Long mainComplaintId);
    List<ComplaintReport> findByMainComplaintIdOrderByCreatedAtAsc(
        Long mainComplaintId);
}
