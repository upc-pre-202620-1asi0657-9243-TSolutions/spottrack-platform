package com.spottrack.platform.profiles.domain.model.valueobjects;

import java.util.regex.Pattern;

public record PhoneNumber(String phoneNumber) {
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{7,15}$");
    public PhoneNumber{
        if (phoneNumber == null || phoneNumber.isBlank()){
            throw new IllegalArgumentException("Phone number must not be null or blank");
        }

        if (!PHONE_PATTERN.matcher(phoneNumber).matches()){
            throw new IllegalArgumentException("Phone number must be a valid format");
        }
    }

    public String getphoneNumber(){return phoneNumber;}
}
