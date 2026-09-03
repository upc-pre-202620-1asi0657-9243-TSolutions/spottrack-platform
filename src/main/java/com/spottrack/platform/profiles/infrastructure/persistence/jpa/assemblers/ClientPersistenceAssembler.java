package com.spottrack.platform.profiles.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.profiles.domain.model.aggregates.Client;
import com.spottrack.platform.profiles.domain.model.valueobjects.Dni;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.model.valueobjects.PersonInfo;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.embeddables.PersonInfoPersistenceEmbeddable;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities.ClientPersistenceEntity;

public final class ClientPersistenceAssembler {

    private ClientPersistenceAssembler() {
    }

    public static Client toDomainFromPersistence(ClientPersistenceEntity entity) {
        return new Client(
                entity.getId(),
                entity.getUserId(),
                toDomainPersonInfo(entity.getPersonInfo()),
                entity.getEmailAddress());
    }

    public static ClientPersistenceEntity toPersistenceFromDomain(Client client) {
        var entity = new ClientPersistenceEntity();
        entity.setId(client.getId());
        entity.setUserId(client.getUserId());
        entity.setPersonInfo(toPersistencePersonInfo(client.getPersonInfo()));
        entity.setEmailAddress(new EmailAddress(client.getEmailAddress()));
        return entity;
    }

    private static PersonInfo toDomainPersonInfo(PersonInfoPersistenceEmbeddable embeddable) {
        if (embeddable == null
                || embeddable.getFirstName() == null
                || embeddable.getLastName() == null
                || embeddable.getPhoneNumber() == null
                || embeddable.getDni() == null) {
            return null;
        }
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
