package com.spottrack.platform.profiles.domain.model.events;

import com.spottrack.platform.profiles.domain.model.aggregates.Admin;

public record AdminRegisteredEvent(
        Long adminId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String dni
) {
    public static AdminRegisteredEvent from(Admin admin) {
        var info = admin.getPersonInfo();
        return new AdminRegisteredEvent(
                admin.getId(),
                info != null ? info.firstName() : null,
                info != null ? info.lastName() : null,
                admin.getEmailAddress(),
                info != null ? info.phoneNumber().phoneNumber() : null,
                info != null ? info.dni().dni() : null
        );
    }
}