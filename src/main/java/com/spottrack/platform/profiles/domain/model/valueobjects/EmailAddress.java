package com.spottrack.platform.profiles.domain.model.valueobjects;

import jakarta.validation.constraints.Email;

import java.util.regex.Pattern;

public record EmailAddress(@Email String address) {
    /**
     * local@domain.tld, written with possessive quantifiers so it runs in linear time.
     * Accepts exactly the same strings as the former {@code ^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$}.
     */
    static final String REGEX =
            "^[A-Za-z0-9+_.-]++@(?!\\.[A-Za-z]{2,}$)(?:[A-Za-z0-9-]*+\\.)++[A-Za-z]{2,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(REGEX);

    public EmailAddress{
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Email address must not be null or blank");

        }

        if (!EMAIL_PATTERN.matcher(address).matches()){
            throw new IllegalArgumentException("Email address must be a valid format");
        }
    }

    public String getAddress(){ return address; }
}
