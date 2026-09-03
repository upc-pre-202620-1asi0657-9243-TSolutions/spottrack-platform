package com.spottrack.platform.reservation.domain.model.events;

public record AlternativeEquipmentRequestedEvent(String requestId, String equipmentId, String reason) {}
