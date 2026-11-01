
package com.makknagar.grievance_management.citizen;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "citizens")
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String citizenReferenceNumber;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String mobileNumber;

    public Citizen() {
    }

    public Citizen(String citizenReferenceNumber, String fullName,
                   String mobileNumber) {
        this.citizenReferenceNumber = citizenReferenceNumber;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
    }

    public Long getId() {
        return id;
    }

    public String getCitizenReferenceNumber() {
        return citizenReferenceNumber;
    }

    public void setCitizenReferenceNumber(String citizenReferenceNumber) {
        this.citizenReferenceNumber = citizenReferenceNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}
