package com.spottrack.platform.iam.interfaces.acl;

/**
 * Data another bounded context hands to IAM to park a business registration until its
 * payment is confirmed. IAM validates it and builds its own command from it, so callers
 * do not depend on the IAM domain model.
 */
public record PendingRegistrationRequest(
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
        String membershipTier) {
}
