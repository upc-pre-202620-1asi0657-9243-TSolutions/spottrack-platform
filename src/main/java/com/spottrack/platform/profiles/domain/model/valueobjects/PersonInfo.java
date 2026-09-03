package com.spottrack.platform.profiles.domain.model.valueobjects;

public record PersonInfo(String firstName, String lastName, PhoneNumber phoneNumber, Dni dni) {

    public PersonInfo{
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("First name must not be null or blank");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Last name must not be null or blank");
        if (phoneNumber == null)
            throw new IllegalArgumentException("Phone number must not be null");
        if (dni == null)
            throw new IllegalArgumentException("DNI must not be null");
    }

    public String getFullName() {
        return "%s %s".formatted(firstName, lastName);
    }
}
