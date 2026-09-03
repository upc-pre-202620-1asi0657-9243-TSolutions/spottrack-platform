package com.spottrack.platform.profiles.domain.model.valueobjects;

import java.util.regex.Pattern;

public record BusinessInfo(
        String companyName,
        String ruc,
        String legalStructure,
        PhoneNumber companyPhone,
        EmailAddress companyEmail,
        String streetAddress,
        String city,
        String district
) {
    private static final Pattern RUC_PATTERN = Pattern.compile("^[0-9]{11}$");

    public BusinessInfo {
        if (companyName == null || companyName.isBlank())
            throw new IllegalArgumentException("Company name must not be null or blank");
        if (ruc == null || ruc.isBlank())
            throw new IllegalArgumentException("RUC must not be null or blank");
        if (!RUC_PATTERN.matcher(ruc).matches())
            throw new IllegalArgumentException("RUC must be 11 digits");
        if (legalStructure == null || legalStructure.isBlank())
            throw new IllegalArgumentException("Legal structure must not be null or blank");
        if (companyPhone == null)
            throw new IllegalArgumentException("Company phone must not be null");
        if (companyEmail == null)
            throw new IllegalArgumentException("Company email must not be null");
        if (streetAddress == null || streetAddress.isBlank())
            throw new IllegalArgumentException("Street address must not be null or blank");
        if (city == null || city.isBlank())
            throw new IllegalArgumentException("City must not be null or blank");
        if (district == null || district.isBlank())
            throw new IllegalArgumentException("District must not be null or blank");
    }
}
