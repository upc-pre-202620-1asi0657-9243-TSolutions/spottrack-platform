package com.spottrack.platform.profiles.domain.model.aggregates;

import com.spottrack.platform.profiles.domain.model.commands.CreateBusinessProfileCommand;
import com.spottrack.platform.profiles.domain.model.events.BusinessProfileCreatedEvent;
import com.spottrack.platform.profiles.domain.model.valueobjects.BusinessInfo;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

public class BusinessProfile extends AbstractDomainAggregateRoot<BusinessProfile> {

    @Getter
    @Setter
    private Long id;

    @Getter
    private Long userId;

    @Getter
    private BusinessInfo businessInfo;

    public BusinessProfile(Long id, Long userId, BusinessInfo businessInfo) {
        this.id = id;
        this.userId = userId;
        this.businessInfo = businessInfo;
    }

    public BusinessProfile(CreateBusinessProfileCommand command) {
        this(null, command.userId(), command.businessInfo());
    }

    public void onCreated() {
        registerDomainEvent(BusinessProfileCreatedEvent.from(this));
    }
}
