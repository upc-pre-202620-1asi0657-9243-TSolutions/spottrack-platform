package com.spottrack.platform.profiles.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.profiles.domain.model.aggregates.Admin;
import com.spottrack.platform.profiles.domain.model.valueobjects.Dni;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.model.valueobjects.PersonInfo;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.embeddables.PersonInfoPersistenceEmbeddable;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities.AdminPersistenceEntity;

public final class AdminPersistenceAssembler {

    private AdminPersistenceAssembler() {
    }

    public static Admin toDomainFromPersistence(AdminPersistenceEntity entity) {
        return new Admin(
                entity.getId(),
                entity.getUserId(),
                toDomainPersonInfo(entity.getPersonInfo()),
                entity.getEmailAddress());
    }

    public static AdminPersistenceEntity toPersistenceFromDomain(Admin admin) {
        var entity = new AdminPersistenceEntity();
        entity.setId(admin.getId());
        entity.setUserId(admin.getUserId());
        entity.setPersonInfo(toPersistencePersonInfo(admin.getPersonInfo()));
        entity.setEmailAddress(new EmailAddress(admin.getEmailAddress()));
        return entity;
    }

    private static PersonInfo toDomainPersonInfo(PersonInfoPersistenceEmbeddable embeddable) {
        if (embeddable == null || embeddable.getFirstName() == null) return null;
        return new PersonInfo(
                embeddable.getFirstName(),
                embeddable.getLastName(),
                new PhoneNumber(embeddable.getPhoneNumber()),
                new Dni(embeddable.getDni()));
    }

    private static PersonInfoPersistenceEmbeddable toPersistencePersonInfo(PersonInfo personInfo) {
        if (personInfo == null) return null;
        return new PersonInfoPersistenceEmbeddable(
                personInfo.firstName(),
                personInfo.lastName(),
                personInfo.phoneNumber().phoneNumber(),
                personInfo.dni().dni());
    }
}
