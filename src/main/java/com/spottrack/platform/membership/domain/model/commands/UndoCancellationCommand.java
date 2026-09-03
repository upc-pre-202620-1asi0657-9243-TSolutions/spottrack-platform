package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;

public record UndoCancellationCommand(MembershipId membershipId) {
    public UndoCancellationCommand {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
    }
}
