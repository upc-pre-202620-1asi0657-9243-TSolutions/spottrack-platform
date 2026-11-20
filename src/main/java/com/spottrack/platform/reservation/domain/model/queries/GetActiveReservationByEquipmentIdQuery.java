package com.spottrack.platform.reservation.domain.model.queries;

public record GetActiveReservationByEquipmentIdQuery(String equipmentId) {
    public GetActiveReservationByEquipmentIdQuery {
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("Equipment id must not be null or blank");
        }
    }
}
