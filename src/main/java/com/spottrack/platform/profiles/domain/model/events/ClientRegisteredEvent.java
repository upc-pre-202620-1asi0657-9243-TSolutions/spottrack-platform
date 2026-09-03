package com.spottrack.platform.profiles.domain.model.events;

import com.spottrack.platform.profiles.domain.model.aggregates.Client;

public record ClientRegisteredEvent(
        Long clientId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String dni
) {
    public static ClientRegisteredEvent from(Client client) {
        var info = client.getPersonInfo();
        return new ClientRegisteredEvent(
                client.getId(),
                info != null ? info.firstName() : null,
                info != null ? info.lastName() : null,
                client.getEmailAddress(),
                info != null ? info.phoneNumber().phoneNumber() : null,
                info != null ? info.dni().dni() : null
        );
    }
}
