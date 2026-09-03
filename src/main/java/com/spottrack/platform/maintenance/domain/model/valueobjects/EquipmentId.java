package com.spottrack.platform.maintenance.domain.model.valueobjects;


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
