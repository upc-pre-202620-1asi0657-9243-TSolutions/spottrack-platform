package com.spottrack.platform.membership.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Owner, company and plan data submitted when registering a business")
public record BusinessRegistrationResource(
        @Schema(description = "Email used to sign in as administrator", example = "owner@spotgym.pe")
        @NotBlank String email,
        @Schema(description = "Account password; stored hashed", example = "S3cure!Pass")
        @NotBlank String password,
        @Schema(description = "Owner first name", example = "Lucia")
        @NotBlank String firstName,
        @Schema(description = "Owner last name", example = "Ramirez")
        @NotBlank String lastName,
        @Schema(description = "Owner phone number", example = "+51987654321")
        @NotBlank String phoneNumber,
        @Schema(description = "Owner national identity document", example = "74125836")
        @NotBlank String dni,
        @Schema(description = "Company name; also used as the name of the gym created after payment",
                example = "Spot Gym Miraflores")
        @NotBlank String companyName,
        @Schema(description = "Company tax identification number", example = "20123456789")
        @NotBlank String ruc,
        @Schema(description = "Legal structure of the company", example = "S.A.C.")
        @NotBlank String legalStructure,
        @Schema(description = "Company phone number", example = "+5114445566")
        @NotBlank String companyPhone,
        @Schema(description = "Company contact email", example = "contacto@spotgym.pe")
        @NotBlank String companyEmail,
        @Schema(description = "Company street address", example = "Av. Larco 123")
        @NotBlank String streetAddress,
        @Schema(description = "Company city", example = "Lima")
        @NotBlank String city,
        @Schema(description = "Company district", example = "Miraflores")
        @NotBlank String district,
        @Schema(description = "Selected plan: BASIC (69 USD), MID (109 USD) or PLATINUM (189 USD); case-insensitive",
                example = "Basic", allowableValues = {"BASIC", "MID", "PLATINUM"})
        @NotBlank String membershipTier
) {}
