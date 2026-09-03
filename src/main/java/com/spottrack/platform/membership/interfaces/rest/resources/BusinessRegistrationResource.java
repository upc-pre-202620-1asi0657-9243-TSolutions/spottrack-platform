package com.spottrack.platform.membership.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record BusinessRegistrationResource(
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phoneNumber,
        @NotBlank String dni,
        @NotBlank String companyName,
        @NotBlank String ruc,
        @NotBlank String legalStructure,
        @NotBlank String companyPhone,
        @NotBlank String companyEmail,
        @NotBlank String streetAddress,
        @NotBlank String city,
        @NotBlank String district,
        @NotBlank String membershipTier
) {}
