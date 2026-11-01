
package com.makknagar.grievance_management.citizen;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    Optional<Citizen> findByCitizenReferenceNumber(
            String citizenReferenceNumber);

    boolean existsByMobileNumber(String mobileNumber);
}
