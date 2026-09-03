package com.spottrack.platform.iam.domain.model.aggregates;

import com.spottrack.platform.iam.domain.model.commands.SavePendingRegistrationCommand;
import com.spottrack.platform.iam.domain.model.valueobjects.PendingRegistrationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class PendingRegistration {

    private UUID registrationId;
    private String email;
    private String hashedPassword;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String dni;
    private String companyName;
    private String ruc;
    private String legalStructure;
    private String companyPhone;
    private String companyEmail;
    private String streetAddress;
    private String city;
    private String district;
    private String membershipTier;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private PendingRegistrationStatus status;

    public PendingRegistration(
            UUID registrationId, String email, String hashedPassword,
            String firstName, String lastName, String phoneNumber, String dni,
            String companyName, String ruc, String legalStructure,
            String companyPhone, String companyEmail,
            String streetAddress, String city, String district,
            String membershipTier, LocalDateTime createdAt,
            LocalDateTime expiresAt, PendingRegistrationStatus status) {
        this.registrationId = registrationId;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.dni = dni;
        this.companyName = companyName;
        this.ruc = ruc;
        this.legalStructure = legalStructure;
        this.companyPhone = companyPhone;
        this.companyEmail = companyEmail;
        this.streetAddress = streetAddress;
        this.city = city;
        this.district = district;
        this.membershipTier = membershipTier;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public PendingRegistration(String hashedPassword, SavePendingRegistrationCommand command) {
        this(
                UUID.randomUUID(),
                command.email(),
                hashedPassword,
                command.firstName(), command.lastName(),
                command.phoneNumber(), command.dni(),
                command.companyName(), command.ruc(), command.legalStructure(),
                command.companyPhone(), command.companyEmail(),
                command.streetAddress(), command.city(), command.district(),
                command.membershipTier(),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30),
                PendingRegistrationStatus.PENDING
        );
    }

    public void consume() {
        if (this.status == PendingRegistrationStatus.CONSUMED)
            throw new IllegalStateException("Pending registration already consumed");
        this.status = PendingRegistrationStatus.CONSUMED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == PendingRegistrationStatus.PENDING;
    }

    public UUID getRegistrationId() { return registrationId; }
    public String getEmail() { return email; }
    public String getHashedPassword() { return hashedPassword; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getDni() { return dni; }
    public String getCompanyName() { return companyName; }
    public String getRuc() { return ruc; }
    public String getLegalStructure() { return legalStructure; }
    public String getCompanyPhone() { return companyPhone; }
    public String getCompanyEmail() { return companyEmail; }
    public String getStreetAddress() { return streetAddress; }
    public String getCity() { return city; }
    public String getDistrict() { return district; }
    public String getMembershipTier() { return membershipTier; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public PendingRegistrationStatus getStatus() { return status; }
}
