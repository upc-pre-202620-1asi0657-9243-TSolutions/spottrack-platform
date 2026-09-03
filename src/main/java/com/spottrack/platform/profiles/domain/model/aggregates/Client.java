package com.spottrack.platform.profiles.domain.model.aggregates;

import com.spottrack.platform.profiles.domain.model.commands.CreateClientCommand;
import com.spottrack.platform.profiles.domain.model.commands.UpdateClientProfileCommand;
import com.spottrack.platform.profiles.domain.model.events.ClientRegisteredEvent;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.model.valueobjects.PersonInfo;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class Client extends AbstractDomainAggregateRoot<Client> {
    @Getter
    @Setter
    private Long id;

    @Getter
    private Long userId;

    @Getter
    private PersonInfo personInfo;

    private EmailAddress emailAddress;

    public Client(Long id, Long userId, PersonInfo personInfo, EmailAddress emailAddress) {
        this.id = id;
        this.userId = userId;
        this.personInfo = personInfo;
        this.emailAddress = Objects.requireNonNull(emailAddress, "Email must not be null");
    }

    public Client(CreateClientCommand command) {
        this(null, command.userId(), null, command.emailAddress());
    }

    public void updateProfile(UpdateClientProfileCommand command) {
        this.personInfo = new PersonInfo(
                command.firstName(),
                command.lastName(),
                command.phoneNumber(),
                command.dni());
    }

    public boolean isProfileComplete() {
        return personInfo != null;
    }

    public PersonInfo getPersonInfo() { return personInfo; }

    public String getFullName() {
        return personInfo != null ? personInfo.getFullName() : "Profile incomplete";
    }

    public String getEmailAddress() { return emailAddress.address(); }

    public void onCreated() {
        registerDomainEvent(ClientRegisteredEvent.from(this));
    }
}
