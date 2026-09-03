package com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.converters.EmailAddressConverter;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.embeddables.PersonInfoPersistenceEmbeddable;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class ClientPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "last_name")),
            @AttributeOverride(name = "phoneNumber", column = @Column(name = "phone_number")),
            @AttributeOverride(name = "dni", column = @Column(name = "dni"))
    })
    private PersonInfoPersistenceEmbeddable personInfo;

    @Convert(converter = EmailAddressConverter.class)
    @Column(name = "email_address", nullable = false, unique = true)
    private EmailAddress emailAddress;

    public ClientPersistenceEntity() {
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public PersonInfoPersistenceEmbeddable getPersonInfo() { return personInfo; }
    public void setPersonInfo(PersonInfoPersistenceEmbeddable personInfo) { this.personInfo = personInfo; }

    public EmailAddress getEmailAddress() { return emailAddress; }
    public void setEmailAddress(EmailAddress emailAddress) { this.emailAddress = emailAddress; }
}
