package com.spottrack.platform.gym.interfaces.rest.resources;

import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;

public record UpdateEquipmentStatusResource(String id, EquipmentStatus status) {

}
