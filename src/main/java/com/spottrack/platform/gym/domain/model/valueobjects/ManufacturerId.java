package com.spottrack.platform.gym.domain.model.valueobjects;


import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public record ManufacturerId( String uuid) {
    private static final String NOT_BLANK= "manufacturer.error.manufacturerId.notBlank";
    private static final String ID_ZERO = "id.error.invalid";
    public ManufacturerId {

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException(NOT_BLANK);
        }

        if (uuid.equals("0")){
            throw new IllegalArgumentException(ID_ZERO);
        }
    }
}
