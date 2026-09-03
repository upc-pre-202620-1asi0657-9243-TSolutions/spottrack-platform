package com.spottrack.platform.gym.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.UUID;



public record EquipmentId(String uuid) {
    private static final String NOT_BLANK= "equipment.error.equipmentId.notBlank";
    private static final String ID_ZERO = "id.error.invalid";
    public EquipmentId {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException(NOT_BLANK);
        }

        if (uuid.isBlank()){
            throw new IllegalArgumentException(ID_ZERO);
        }


    }
}
