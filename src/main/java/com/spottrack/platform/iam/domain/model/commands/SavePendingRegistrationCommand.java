package com.spottrack.platform.iam.domain.model.commands;

public record SavePendingRegistrationCommand(
        String email,
        String rawPassword,
        String firstName,
        String lastName,
        String phoneNumber,
        String dni,
        String companyName,
        String ruc,
        String legalStructure,
        String companyPhone,
        String companyEmail,
        String streetAddress,
        String city,
        String district,
        String membershipTier
) {
    public SavePendingRegistrationCommand {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email must not be blank");
        if (rawPassword == null || rawPassword.isBlank())
            throw new IllegalArgumentException("Password must not be blank");
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("First name must not be blank");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Last name must not be blank");
        if (phoneNumber == null || phoneNumber.isBlank())
            throw new IllegalArgumentException("Phone number must not be blank");
        if (dni == null || dni.isBlank())
            throw new IllegalArgumentException("DNI must not be blank");
        if (companyName == null || companyName.isBlank())
            throw new IllegalArgumentException("Company name must not be blank");
        if (ruc == null || ruc.isBlank())
            throw new IllegalArgumentException("RUC must not be blank");
        if (legalStructure == null || legalStructure.isBlank())
            throw new IllegalArgumentException("Legal structure must not be blank");
        if (companyPhone == null || companyPhone.isBlank())
            throw new IllegalArgumentException("Company phone must not be blank");
        if (companyEmail == null || companyEmail.isBlank())
            throw new IllegalArgumentException("Company email must not be blank");
        if (streetAddress == null || streetAddress.isBlank())
            throw new IllegalArgumentException("Street address must not be blank");
        if (city == null || city.isBlank())
            throw new IllegalArgumentException("City must not be blank");
        if (district == null || district.isBlank())
            throw new IllegalArgumentException("District must not be blank");
        if (membershipTier == null || membershipTier.isBlank())
            throw new IllegalArgumentException("Membership tier must not be blank");
    }
}
