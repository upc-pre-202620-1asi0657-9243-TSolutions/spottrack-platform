package com.spottrack.platform.gym.domain.model.commands;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;

public record UpdateEquipmentStatus(String id, EquipmentStatus status) {
    public UpdateEquipmentStatus{
        if (id == null){
            throw new IllegalArgumentException();
        }
    }
}
