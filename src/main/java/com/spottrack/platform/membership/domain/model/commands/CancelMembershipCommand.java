package com.spottrack.platform.membership.domain.model.commands;

import com.spottrack.platform.membership.domain.model.valueobjects.MembershipId;

public record CancelMembershipCommand(MembershipId membershipId) {
    public CancelMembershipCommand {
        if (membershipId == null) throw new IllegalArgumentException("membership.error.membershipId.notNull");
    }
}
