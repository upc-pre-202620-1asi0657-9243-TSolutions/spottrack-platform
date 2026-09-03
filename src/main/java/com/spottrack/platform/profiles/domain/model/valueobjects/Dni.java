package com.spottrack.platform.profiles.domain.model.valueobjects;

import java.util.regex.Pattern;

public record Dni(String dni) {
    private static final Pattern DNI_PATTERN =
            Pattern.compile("^[0-9]{8}$");
    public Dni {
        if (dni == null || dni.isBlank()){
            throw new IllegalArgumentException("DNI must not be null or blank");
        }

        if (!DNI_PATTERN.matcher(dni).matches()){
            throw new IllegalArgumentException("DNI must be a valid format");
        }
    }

    public String getDNI(){return dni;}
}