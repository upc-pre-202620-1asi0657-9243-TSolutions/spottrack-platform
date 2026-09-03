package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;

import java.time.LocalDate;

public record RenewMembershipCommand(MembershipId membershipId, LocalDate startDate, LocalDate endDate) {
    public RenewMembershipCommand {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
        if (startDate == null) throw new IllegalArgumentException("membership.error.startDate.notNull");
        if (endDate == null) throw new IllegalArgumentException("membership.error.endDate.notNull");
    }
}
