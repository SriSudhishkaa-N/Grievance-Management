
package com.makknagar.grievance_management.citizen;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/citizens")
public class CitizenController {

    private final CitizenService citizenService;

    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCitizen(
            @RequestBody Map<String, String> request) {

        try {
            String name = request.get("fullName");
            String mobile = request.get("mobile");

            Citizen citizen = citizenService.registerCitizen(name, mobile);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Registration successful!",
                    "citizenReferenceNumber",
                    citizen.getCitizenReferenceNumber(),
                    "fullName", citizen.getFullName()
            ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", exception.getMessage()
            ));
        }
    }
}
