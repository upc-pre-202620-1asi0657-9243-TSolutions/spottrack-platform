package com.spottrack.platform.profiles.domain.model.aggregates;

import com.spottrack.platform.profiles.domain.model.commands.CreateAdminCommand;
import com.spottrack.platform.profiles.domain.model.commands.UpdateAdminProfileCommand;
import com.spottrack.platform.profiles.domain.model.events.AdminRegisteredEvent;
import com.spottrack.platform.profiles.domain.model.valueobjects.Dni;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.model.valueobjects.PersonInfo;
import com.spottrack.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class Admin extends AbstractDomainAggregateRoot<Admin> {
    @Getter
    @Setter
    private Long id;

    @Getter
    private Long userId;

    @Getter
    private PersonInfo personInfo;

    private EmailAddress emailAddress;

    public Admin(Long id, Long userId, PersonInfo personInfo, EmailAddress emailAddress) {
        this.id = id;
        this.userId = userId;
        this.personInfo = personInfo;
        this.emailAddress = Objects.requireNonNull(emailAddress, "Email must not be null");
    }

    public Admin(CreateAdminCommand command) {
        this(null, command.userId(), buildPersonInfo(command), command.emailAddress());
    }

    private static PersonInfo buildPersonInfo(CreateAdminCommand command) {
        if (command.firstName() == null || command.lastName() == null
                || command.phoneNumber() == null || command.dni() == null) return null;
        return new PersonInfo(command.firstName(), command.lastName(),
                new PhoneNumber(command.phoneNumber()), new Dni(command.dni()));
    }

    public void updateProfile(UpdateAdminProfileCommand command) {
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
        registerDomainEvent(AdminRegisteredEvent.from(this));
    }
}
