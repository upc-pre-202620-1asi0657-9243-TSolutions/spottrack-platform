package com.spottrack.platform.gym.domain.model.queries;

public record GetAvailableAlternativesQuery(String equipmentName, String excludeEquipmentId) {
}
