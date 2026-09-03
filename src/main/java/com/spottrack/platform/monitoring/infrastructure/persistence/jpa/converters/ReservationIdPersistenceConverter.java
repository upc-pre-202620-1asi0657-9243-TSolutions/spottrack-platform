package com.spottrack.platform.monitoring.infrastructure.persistence.jpa.converters;

import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ReservationIdPersistenceConverter implements AttributeConverter<ReservationId, String> {
    @Override
    public String convertToDatabaseColumn(ReservationId attribute) {
        return attribute == null ? null : attribute.uuid();
    }

    @Override
    public ReservationId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new ReservationId(dbData);
    }
}
