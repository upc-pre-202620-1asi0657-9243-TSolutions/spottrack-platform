package com.spottrack.platform.profiles.infrastructure.persistence.jpa.converters;

import com.spottrack.platform.profiles.domain.model.valueobjects.Dni;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class DNIConverter implements AttributeConverter<Dni, String> {

    @Override
    public String convertToDatabaseColumn(Dni attribute) {
        return attribute == null ? null : attribute.dni();
    }

    @Override
    public Dni convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new Dni(dbData);
    }
}
