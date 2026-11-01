
package com.makknagar.grievance_management.citizen;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitizenService {

    private final CitizenRepository citizenRepository;

    public CitizenService(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    @Transactional
    public Citizen registerCitizen(String fullName, String mobileNumber) {

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (mobileNumber == null || !mobileNumber.matches("[0-9]{10}")) {
            throw new IllegalArgumentException(
                    "Enter a valid 10-digit mobile number.");
        }

        if (citizenRepository.existsByMobileNumber(mobileNumber)) {
            throw new IllegalArgumentException(
                    "This mobile number is already registered.");
        }

        Citizen citizen = new Citizen(
                "TEMP",
                fullName.trim(),
                mobileNumber
        );

        Citizen savedCitizen = citizenRepository.save(citizen);

        String referenceNumber = String.format(
                "CIT-%04d", savedCitizen.getId());

        savedCitizen.setCitizenReferenceNumber(referenceNumber);

        return citizenRepository.save(savedCitizen);
    }
}
